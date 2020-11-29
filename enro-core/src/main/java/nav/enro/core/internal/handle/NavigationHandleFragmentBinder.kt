package nav.enro.core.internal.handle

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import nav.enro.core.*
import nav.enro.core.context.FragmentContext
import nav.enro.core.context.leafContext
import nav.enro.core.context.navigationContext
import nav.enro.core.controller.navigationController
import nav.enro.core.internal.navigationHandle
import java.util.*

internal object NavigationHandleFragmentBinder: FragmentManager.FragmentLifecycleCallbacks() {
    override fun onFragmentCreated(fm: FragmentManager, fragment: Fragment, savedInstanceState: Bundle?) {
        val instruction = fragment.arguments?.readOpenInstruction()
        val contextId = instruction?.instructionId
            ?: savedInstanceState?.getString(CONTEXT_ID_ARG)
            ?: UUID.randomUUID().toString()

        val config = NavigationHandleProperty.getPendingConfig(fragment)
        val defaultInstruction = NavigationInstruction.Open(
            instructionId = contextId,
            navigationDirection = NavigationDirection.FORWARD,
            navigationKey = config?.defaultKey ?: NoNavigationKeyBound(fragment::class.java, fragment.arguments)
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
        f.requireActivity().application.navigationController.active = f.requireActivity().navigationContext.leafContext().navigationHandle()
    }


}