package nav.enro.core.internal.handle

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import nav.enro.core.NavigationKey
import nav.enro.core.context.ActivityContext
import nav.enro.core.context.leafContext
import nav.enro.core.context.navigationContext
import nav.enro.core.controller.navigationController
import nav.enro.core.internal.navigationHandle

internal object NavigationHandleActivityBinder : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity !is FragmentActivity) return

        activity.theme.applyStyle(android.R.style.Animation_Activity, false)
        activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
            NavigationHandleFragmentBinder, true
        )

        val handle by activity.viewModels<NavigationHandleViewModel<NavigationKey>> { ViewModelProvider.NewInstanceFactory() }
        handle.navigationContext = ActivityContext(activity)
        if(savedInstanceState  == null) handle.executeDeeplink()
        activity.findViewById<ViewGroup>(android.R.id.content).viewTreeObserver.addOnGlobalLayoutListener {
            activity.application.navigationController.active = activity.navigationContext.leafContext().navigationHandle()
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityDestroyed(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {
        if(activity !is FragmentActivity) return
        activity.application.navigationController.active = activity.navigationContext.leafContext().navigationHandle()
    }
}