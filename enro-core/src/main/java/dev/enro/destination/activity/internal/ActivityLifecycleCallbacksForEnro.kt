package dev.enro.destination.activity.internal

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import dev.enro.compatability.interceptBackPressForAndroidxNavigation
import dev.enro.core.controller.usecase.OnNavigationContextCreated
import dev.enro.core.controller.usecase.OnNavigationContextSaved
import dev.enro.core.getNavigationHandle
import dev.enro.core.leafContext
import dev.enro.core.requestClose
import dev.enro.destination.activity.ActivityContext
import dev.enro.destination.activity.navigationContext

internal class ActivityLifecycleCallbacksForEnro(
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