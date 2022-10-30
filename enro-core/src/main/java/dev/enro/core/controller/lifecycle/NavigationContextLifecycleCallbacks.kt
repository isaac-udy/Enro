package dev.enro.core.controller.lifecycle

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.core.view.ViewCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import dev.enro.core.*
import dev.enro.core.container.NavigationContainerProperty
import dev.enro.core.fragment.container.FragmentPresentationContainer
import dev.enro.core.internal.handle.getNavigationHandleViewModel
import dev.enro.core.internal.handle.interceptBackPressForAndroidxNavigation

internal class NavigationContextLifecycleCallbacks(
    private val lifecycleController: NavigationLifecycleController
) {

    private val fragmentCallbacks = FragmentCallbacks()
    private val activityCallbacks = ActivityCallbacks()

    fun install(application: Application) {
        application.registerActivityLifecycleCallbacks(activityCallbacks)
    }

    internal fun uninstall(application: Application) {
        application.unregisterActivityLifecycleCallbacks(activityCallbacks)
    }

    inner class ActivityCallbacks : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(
            activity: Activity,
            savedInstanceState: Bundle?
        ) {
            if (activity !is ComponentActivity) return

            val navigationContext = ActivityContext(activity)

            if (activity is FragmentActivity) {
                activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
                    fragmentCallbacks,
                    true
                )

                NavigationContainerProperty(
                    lifecycleOwner = activity,
                    navigationContainerProducer = {
                        FragmentPresentationContainer(
                            parentContext = activity.navigationContext,
                        )
                    }
                )
            }

            lifecycleController.onContextCreated(navigationContext, savedInstanceState)

            activity.onBackPressedDispatcher.addCallback(activity) {
                val leafContext = navigationContext.leafContext()
                if (interceptBackPressForAndroidxNavigation(this, leafContext)) return@addCallback
                leafContext.getNavigationHandleViewModel().requestClose()
            }
        }

        override fun onActivitySaveInstanceState(
            activity: Activity,
            outState: Bundle
        ) {
            if (activity !is ComponentActivity) return
            lifecycleController.onContextSaved(activity.navigationContext, outState)
        }

        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityResumed(activity: Activity) {}
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivityDestroyed(activity: Activity) {}
    }

    inner class FragmentCallbacks : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentPreCreated(
            fm: FragmentManager,
            fragment: Fragment,
            savedInstanceState: Bundle?
        ) {
            // TODO throw exception if fragment is opened into an Enro registered NavigationContainer without
            // being opened through Enro
            lifecycleController.onContextCreated(FragmentContext(fragment), savedInstanceState)
            NavigationContainerProperty(
                lifecycleOwner = fragment,
                navigationContainerProducer = {
                    FragmentPresentationContainer(
                        parentContext = fragment.navigationContext,
                    )
                }
            )
        }

        override fun onFragmentSaveInstanceState(
            fm: FragmentManager,
            fragment: Fragment,
            outState: Bundle
        ) {
            lifecycleController.onContextSaved(fragment.navigationContext, outState)
        }

        override fun onFragmentViewCreated(
            fm: FragmentManager,
            fragment: Fragment,
            view: View,
            outState: Bundle?
        ) {
            if (fragment is DialogFragment && fragment.showsDialog) {
                ViewCompat.addOnUnhandledKeyEventListener(view, DialogFragmentBackPressedListener)
            }
        }
    }
}

private object DialogFragmentBackPressedListener : ViewCompat.OnUnhandledKeyEventListenerCompat {
    override fun onUnhandledKeyEvent(view: View, event: KeyEvent): Boolean {
        val isBackPressed =
            event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP
        if (!isBackPressed) return false

        view.findViewTreeViewModelStoreOwner()
            ?.getNavigationHandleViewModel()
            ?.navigationContext
            ?.leafContext()
            ?.getNavigationHandle()
            ?.requestClose() ?: return false

        return true
    }
}