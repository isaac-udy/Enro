package nav.enro.core.activity

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import nav.enro.core.*
import nav.enro.core.ActivityContext
import nav.enro.core.leafContext
import nav.enro.core.navigationContext
import nav.enro.core.internal.NoNavigationKey
import nav.enro.core.internal.handle.createNavigationHandleViewModel
import java.util.*

internal object NavigationHandleActivityBinder : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity !is FragmentActivity) return

        activity.theme.applyStyle(android.R.style.Animation_Activity, false)

        val instruction = activity.intent.extras?.readOpenInstruction()
        val contextId = instruction?.instructionId
            ?: savedInstanceState?.getString(CONTEXT_ID_ARG)
            ?: UUID.randomUUID().toString()

        val config = NavigationHandleProperty.getPendingConfig(activity)
        val defaultInstruction = NavigationInstruction.Open(
            instructionId = contextId,
            navigationDirection = NavigationDirection.FORWARD,
            navigationKey = config?.defaultKey ?: NoNavigationKey(activity::class.java, activity.intent.extras)
        )

        val handle = activity.createNavigationHandleViewModel(
            activity.application.navigationController,
            instruction ?: defaultInstruction
        )
        config?.applyTo(handle)

        handle.navigationContext = ActivityContext(activity)
        if(savedInstanceState  == null) handle.executeDeeplink()
        activity.findViewById<ViewGroup>(android.R.id.content).viewTreeObserver.addOnGlobalLayoutListener {
            activity.application.navigationController.active = activity.navigationContext.leafContext().getNavigationHandleViewModel()
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        if (activity !is FragmentActivity) return
        outState.putString(CONTEXT_ID_ARG, activity.getNavigationHandle().id)
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityDestroyed(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {
        if(activity !is FragmentActivity) return
        activity.application.navigationController.active = activity.navigationContext.leafContext().getNavigationHandleViewModel()
    }
}