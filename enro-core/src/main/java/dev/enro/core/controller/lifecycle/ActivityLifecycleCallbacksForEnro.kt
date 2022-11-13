package dev.enro.core.controller.lifecycle

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import dev.enro.core.container.NavigationContainerProperty
import dev.enro.core.getNavigationHandle
import dev.enro.core.internal.handle.interceptBackPressForAndroidxNavigation
import dev.enro.core.leafContext
import dev.enro.core.navigationContext
import dev.enro.core.requestClose
import dev.enro.core.usecase.OnNavigationContextCreated
import dev.enro.core.usecase.OnNavigationContextSaved
import dev.enro.fragment.container.FragmentPresentationContainer

internal class ActivityLifecycleCallbacksForEnro(
    private val onNavigationContextCreated: OnNavigationContextCreated,
    private val onNavigationContextSaved: OnNavigationContextSaved,
    private val fragmentLifecycleCallbacks: FragmentManager.FragmentLifecycleCallbacks
) : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(
        activity: Activity,
        savedInstanceState: Bundle?
    ) {
        if (activity !is ComponentActivity) return

        val navigationContext = dev.enro.activity.ActivityContext(activity)

        if (activity is FragmentActivity) {
            activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
                fragmentLifecycleCallbacks,
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

        onNavigationContextCreated(navigationContext, savedInstanceState)

        activity.onBackPressedDispatcher.addCallback(activity) {
            val leafContext = navigationContext.leafContext()
            if (interceptBackPressForAndroidxNavigation(this, leafContext)) return@addCallback
            leafContext.getNavigationHandle().requestClose()
        }
    }

    override fun onActivitySaveInstanceState(
        activity: Activity,
        outState: Bundle
    ) {
        if (activity !is ComponentActivity) return
        onNavigationContextSaved(activity.navigationContext, outState)
    }

    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivityDestroyed(activity: Activity) {}
}