package dev.enro.destination.fragment

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.activity.ComponentDialog
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationContainerProperty
import dev.enro.core.container.emptyBackstack
import dev.enro.core.containerManager
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.application
import dev.enro.core.controller.get
import dev.enro.core.controller.isInAndroidContext
import dev.enro.core.controller.usecase.OnNavigationContextCreated
import dev.enro.core.controller.usecase.OnNavigationContextSaved
import dev.enro.core.fragment.container.FragmentNavigationContainer
import dev.enro.core.getNavigationHandle
import dev.enro.core.internal.handle.getNavigationHandleViewModel
import dev.enro.core.leafContext
import dev.enro.core.navigationContext
import dev.enro.core.plugins.EnroPlugin
import dev.enro.core.requestClose

internal object FragmentPlugin : EnroPlugin() {
    private var callbacks: FragmentLifecycleCallbacksForEnro? = null

    override fun onAttached(navigationController: NavigationController) {
        if (!navigationController.isInAndroidContext) return

        callbacks = FragmentLifecycleCallbacksForEnro(
            navigationController.dependencyScope.get(),
            navigationController.dependencyScope.get(),
        ).also { callbacks ->
            navigationController.application.registerActivityLifecycleCallbacks(callbacks)
        }
    }

    override fun onDetached(navigationController: NavigationController) {
        if (!navigationController.isInAndroidContext) return

        callbacks?.let { callbacks ->
            navigationController.application.unregisterActivityLifecycleCallbacks(callbacks)
        }
        callbacks = null
    }
}

private class FragmentLifecycleCallbacksForEnro(
    private val onNavigationContextCreated: OnNavigationContextCreated,
    private val onNavigationContextSaved: OnNavigationContextSaved,
) : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity !is FragmentActivity)  return
        activity.supportFragmentManager
            .registerFragmentLifecycleCallbacks(fragmentCallbacks, true)

        NavigationContainerProperty(
            lifecycleOwner = activity,
            navigationContainerProducer = {
                FragmentNavigationContainer(
                    containerId = android.R.id.content,
                    parentContext = activity.navigationContext,
                    accept = { false },
                    emptyBehavior = EmptyBehavior.AllowEmpty,
                    interceptor = {},
                    animations = {},
                    initialBackstack = emptyBackstack(),
                )
            },
            onContainerAttached = {
                if (activity.containerManager.activeContainer != it) return@NavigationContainerProperty
                if (savedInstanceState != null) return@NavigationContainerProperty
                activity.containerManager.setActiveContainer(null)
            }
        )
    }

    private val fragmentCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentPreCreated(
            fm: FragmentManager,
            fragment: Fragment,
            savedInstanceState: Bundle?
        ) {
            // TODO throw exception if fragment is opened into an Enro registered NavigationContainer without
            // being opened through Enro
            onNavigationContextCreated(FragmentContext(fragment), savedInstanceState)
        }

        override fun onFragmentSaveInstanceState(
            fm: FragmentManager,
            fragment: Fragment,
            outState: Bundle
        ) {
            onNavigationContextSaved(fragment.navigationContext, outState)
        }

        override fun onFragmentViewCreated(
            fm: FragmentManager,
            fragment: Fragment,
            view: View,
            outState: Bundle?
        ) {
            if (fragment !is DialogFragment || !fragment.showsDialog) return

            val dialog = fragment.requireDialog()
            when (dialog) {
                is ComponentDialog -> dialog.onBackPressedDispatcher.addCallback(
                    fragment.viewLifecycleOwner,
                    object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            fragment.navigationContext.leafContext().getNavigationHandle()
                                .requestClose()
                        }
                    }
                )
                else -> ViewCompat.addOnUnhandledKeyEventListener(
                    view,
                    DialogFragmentBackPressedListener
                )
            }
        }
    }

    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}

private object DialogFragmentBackPressedListener : ViewCompat.OnUnhandledKeyEventListenerCompat {
    override fun onUnhandledKeyEvent(view: View, event: KeyEvent): Boolean {
        val isBackPressed = event.keyCode == KeyEvent.KEYCODE_BACK &&
                event.action == KeyEvent.ACTION_UP

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