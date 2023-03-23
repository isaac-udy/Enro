package dev.enro.core.controller.lifecycle

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import dev.enro.core.*
import dev.enro.core.compatability.interceptBackPressForAndroidxNavigation
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationContainerProperty
import dev.enro.core.container.emptyBackstack
import dev.enro.core.controller.usecase.OnNavigationContextCreated
import dev.enro.core.controller.usecase.OnNavigationContextSaved
import dev.enro.core.fragment.container.FragmentNavigationContainer

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

        val navigationContext = ActivityContext(activity)

        if (activity is FragmentActivity) {
            activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
                fragmentLifecycleCallbacks,
                true
            )

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
                    ).also {
                        if (activity.containerManager.activeContainer != it) return@also
                        if (savedInstanceState != null) return@also
                        activity.containerManager.setActiveContainer(null)
                    }
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