package nav.enro.core.internal.handle

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import nav.enro.core.CONTEXT_ID_ARG
import nav.enro.core.context.FragmentContext
import nav.enro.core.NavigationKey
import nav.enro.core.context.leafContext
import nav.enro.core.context.navigationContext
import nav.enro.core.controller.navigationController
import nav.enro.core.internal.navigationHandle
import nav.enro.core.readOpenInstruction
import java.util.*

internal object NavigationHandleFragmentBinder: FragmentManager.FragmentLifecycleCallbacks() {
    override fun onFragmentCreated(fm: FragmentManager, fragment: Fragment, savedInstanceState: Bundle?) {
        val handle = fragment.viewModels<NavigationHandleViewModel<NavigationKey>> { ViewModelProvider.NewInstanceFactory() } .value

        val contextId = fragment.arguments?.readOpenInstruction<NavigationKey>()?.instructionId
            ?: savedInstanceState?.getString(CONTEXT_ID_ARG)
            ?: UUID.randomUUID().toString()

        handle.navigationContext = FragmentContext(fragment, contextId)
        if(savedInstanceState == null) handle.executeDeeplink()
    }

    override fun onFragmentSaveInstanceState(fm: FragmentManager, fragment: Fragment, outState: Bundle) {
        outState.putString(CONTEXT_ID_ARG, fragment.navigationContext.id)
    }

    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
        f.requireActivity().application.navigationController.active = f.requireActivity().navigationContext.leafContext().navigationHandle()
    }


}