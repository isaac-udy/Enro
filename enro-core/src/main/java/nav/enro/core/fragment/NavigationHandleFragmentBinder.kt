package nav.enro.core.fragment

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import nav.enro.core.*
import nav.enro.core.internal.NoNavigationKey
import nav.enro.core.internal.handle.createNavigationHandleViewModel
import java.util.*

internal object NavigationHandleFragmentBinder: Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if(activity !is FragmentActivity) return
        activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
            FragmentCallbacks, true
        )
    }

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}

    private object FragmentCallbacks: FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentCreated(fm: FragmentManager, fragment: Fragment, savedInstanceState: Bundle?) {
            val instruction = fragment.arguments?.readOpenInstruction()
            val contextId = instruction?.instructionId
                ?: savedInstanceState?.getString(CONTEXT_ID_ARG)
                ?: UUID.randomUUID().toString()

            val config = NavigationHandleProperty.getPendingConfig(fragment)
            val defaultInstruction = NavigationInstruction.Open(
                instructionId = contextId,
                navigationDirection = NavigationDirection.FORWARD,
                navigationKey = config?.defaultKey ?: NoNavigationKey(fragment::class.java, fragment.arguments)
            )

            val handle = fragment.createNavigationHandleViewModel(
                fragment.requireActivity().application.navigationController,
                instruction ?: defaultInstruction
            )
            config?.applyTo(handle)

            handle.navigationContext = FragmentContext(fragment)
            if(savedInstanceState == null) handle.executeDeeplink()
        }

        override fun onFragmentSaveInstanceState(fm: FragmentManager, fragment: Fragment, outState: Bundle) {
            outState.putString(CONTEXT_ID_ARG, fragment.getNavigationHandle().id)
        }

        override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
            f.requireActivity().application.navigationController.active = f.requireActivity().navigationContext.leafContext().getNavigationHandleViewModel()
        }
    }
}
