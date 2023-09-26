package dev.enro.destination.fragment.container

import android.os.Bundle
import android.util.Log
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContext
import dev.enro.core.container.NavigationBackstack
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.close
import dev.enro.core.container.components.ContainerContextProvider
import dev.enro.core.container.components.ContainerState
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.HostInstructionAs
import dev.enro.core.fragment.container.FragmentFactory
import dev.enro.core.fragment.container.fragmentManager
import dev.enro.core.navigationContext
import dev.enro.extensions.getParcelableCompat

internal class FragmentContextProvider(
    private val containerId: Int,
    private val context: NavigationContext<*>,
) : ContainerContextProvider<Fragment>, NavigationContainer.Component {

    private val hostInstructionAs = context.controller.dependencyScope.get<HostInstructionAs>()
    internal val ownedFragments = mutableSetOf<String>()
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
                val state = boundState ?: return
                state.setBackstack(state.backstack.close(instructionId))
            }
        }, false)
    }

    private var boundState: ContainerState? = null

    override fun getActiveNavigationContext(backstack: NavigationBackstack): NavigationContext<Fragment>? {
        val fragment = backstack.lastOrNull()
            ?.let { fragmentManager.findFragmentByTag(it.instructionId) }
            ?: fragmentManager.findFragmentById(containerId)
        val result = fragment?.navigationContext
        Log.e("Rendered", "getActiveNContext: ${result?.lifecycle?.currentState} [${backstack.joinToString { it.navigationKey::class.java.simpleName }}]")
        return result
    }

    override fun getContext(instruction: AnyOpenInstruction): Fragment? {
        return fragmentManager.findFragmentByTag(instruction.instructionId)
    }

    override fun createContext(instruction: AnyOpenInstruction): Fragment {
        val hostedType = when (containerId) {
            android.R.id.content -> DialogFragment::class.java
            else -> Fragment::class.java
        }

        val fragment = FragmentFactory.createFragment(
            parentContext = context,
            instruction = hostInstructionAs(hostedType, context, instruction)
        )

        val restoredState = restoredFragmentStates.remove(instruction.instructionId)
        if (restoredState != null) fragment.setInitialSavedState(restoredState)
        return fragment
    }

    override fun create(state: ContainerState) {
        boundState = state
    }

    override fun save(): Bundle {
        val state = boundState ?: return Bundle.EMPTY
        val savedState = bundleOf()
        state.backstack
            .mapNotNull { it.withFragment(::getContext) }
            .forEach { (instruction, fragment) ->
                val fragmentState = fragmentManager.saveFragmentInstanceState(fragment)
                savedState.putParcelable(
                    "${FRAGMENT_STATE_PREFIX_KEY}${instruction.instructionId}",
                    fragmentState
                )
            }
        savedState.putStringArrayList(OWNED_FRAGMENTS_KEY, ArrayList(ownedFragments))
        return savedState
    }

    override fun restore(bundle: Bundle) {
        bundle.keySet().forEach { key ->
            if (!key.startsWith(FRAGMENT_STATE_PREFIX_KEY)) return@forEach
            val fragmentState = bundle.getParcelableCompat<Fragment.SavedState>(key) ?: return@forEach
            val instructionId = key.removePrefix(FRAGMENT_STATE_PREFIX_KEY)
            restoredFragmentStates[instructionId] = fragmentState
        }
        ownedFragments.addAll(bundle.getStringArrayList(OWNED_FRAGMENTS_KEY).orEmpty())
        super.restore(bundle)
    }

    override fun destroy() {
        boundState = null
    }

    private companion object {
        private const val FRAGMENT_STATE_PREFIX_KEY = "FragmentState@"
        private const val OWNED_FRAGMENTS_KEY = "OWNED_FRAGMENTS_KEY"
    }
}

internal data class InstructionWithFragment(
    val instruction: AnyOpenInstruction,
    val fragment: Fragment,
)

internal fun AnyOpenInstruction?.withFragment(
    block: (AnyOpenInstruction) -> Fragment?
): InstructionWithFragment? {
    this ?: return null
    val fragment = block(this)
        ?: return null
    return InstructionWithFragment(
        instruction = this,
        fragment = fragment
    )
}