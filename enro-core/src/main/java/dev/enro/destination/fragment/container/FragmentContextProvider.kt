package dev.enro.destination.fragment.container

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContext
import dev.enro.core.container.NavigationBackstack
import dev.enro.core.container.components.ContainerContextProvider
import dev.enro.core.container.components.ContainerState
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.HostInstructionAs
import dev.enro.core.fragment.container.FragmentFactory
import dev.enro.core.fragment.container.fragmentManager
import dev.enro.core.navigationContext

internal class FragmentContextProvider(
    private val containerId: Int,
    private val context: NavigationContext<*>,
) : ContainerContextProvider<Fragment> {

    private val hostInstructionAs = context.controller.dependencyScope.get<HostInstructionAs>()
    private val ownedFragments = mutableSetOf<String>()
    private val restoredFragmentStates = mutableMapOf<String, Fragment.SavedState>()
    private val fragmentManager = context.fragmentManager

    init {
        fragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
                if (f !is DialogFragment) return
                val instructionId = f.tag ?: return
                if (fm.isDestroyed || fm.isStateSaved) return
                if (!f.isRemoving) return
                ownedFragments.remove(f.tag)

                // TODO: setBackstack(backstack.close(instructionId))
            }
        }, false)
    }

    private var boundState: ContainerState? = null

    override fun getActiveNavigationContext(backstack: NavigationBackstack): NavigationContext<Fragment>? {
        val fragment = backstack.lastOrNull()
            ?.let { fragmentManager.findFragmentByTag(it.instructionId) }
            ?: fragmentManager.findFragmentById(containerId)
        return fragment?.navigationContext
    }

    override fun getContext(instruction: AnyOpenInstruction): Fragment? {
        return fragmentManager.findFragmentByTag(instruction.instructionId)
    }

    override fun createContext(instruction: AnyOpenInstruction): Fragment {
        // TODO: DialogFragments
        /*
        val cls = when (containerId) {
                    android.R.id.content -> DialogFragment::class.java
                    else -> Fragment::class.java
                }
         */
        val fragment = FragmentFactory.createFragment(
            parentContext = context,
            instruction = hostInstructionAs(Fragment::class.java, context, instruction)
        )

        val restoredState = restoredFragmentStates.remove(instruction.instructionId)
        if (restoredState != null) fragment.setInitialSavedState(restoredState)
        return fragment
    }

    override fun bind(state: ContainerState) {
        boundState = state
    }

    override fun destroy() {
        boundState = null
    }
}