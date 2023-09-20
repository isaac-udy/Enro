package dev.enro.destination.activity

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import dev.enro.compatability.interceptBackPressForAndroidxNavigation
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.application
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.OnNavigationContextCreated
import dev.enro.core.controller.usecase.OnNavigationContextSaved
import dev.enro.core.getNavigationHandle
import dev.enro.core.leafContext
import dev.enro.core.navigationContext
import dev.enro.core.plugins.EnroPlugin
import dev.enro.core.requestClose

internal object ActivityPlugin : EnroPlugin() {

    private var callbacks: ActivityLifecycleCallbacksForEnro? = null

    override fun onAttached(navigationController: NavigationController) {
        callbacks = ActivityLifecycleCallbacksForEnro(
            navigationController.dependencyScope.get(),
            navigationController.dependencyScope.get(),
        ).also { callbacks ->
            navigationController.application.registerActivityLifecycleCallbacks(callbacks)
        }
    }

    override fun onDetached(navigationController: NavigationController) {
        callbacks?.let { callbacks ->
            navigationController.application.unregisterActivityLifecycleCallbacks(callbacks)
        }
        callbacks = null
    }
}

private class ActivityLifecycleCallbacksForEnro(
    private val onNavigationContextCreated: OnNavigationContextCreated,
    private val onNavigationContextSaved: OnNavigationContextSaved,
) : Application.ActivityLifecycleCallbacks {

    override fun onActivityCreated(
        activity: Activity,
        savedInstanceState: Bundle?
    ) {
        if (activity !is ComponentActivity) return

        val navigationContext = ActivityContext(activity)

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

    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}