package nav.enro.core.internal.handle

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import nav.enro.core.NavigationKey
import nav.enro.core.internal.context.ActivityContext

internal object NavigationHandleActivityBinder : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity !is FragmentActivity) return

        activity.theme.applyStyle(android.R.style.Animation_Activity, false)
        activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
            NavigationHandleFragmentBinder, true
        )

        val handle by activity.viewModels<NavigationHandleViewModel<NavigationKey>>()
        handle.navigationContext = ActivityContext(activity = activity)
        if(savedInstanceState != null) handle.executeDeeplink()
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityDestroyed(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
}