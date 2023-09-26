package dev.enro.destination.fragment.container

import android.app.Activity
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commitNow
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withCreated
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationDirection
import dev.enro.core.container.NavigationBackstack
import dev.enro.core.container.NavigationBackstackTransition
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.components.ContainerRenderer
import dev.enro.core.container.components.ContainerState
import dev.enro.core.container.components.getOrCreateContext
import dev.enro.core.container.emptyBackstack
import dev.enro.core.fragment.container.fragmentManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class FragmentContainerRenderer(
    private val containerId: Int,
    private val context: NavigationContext<out Any>,
    private val contextProvider: FragmentContextProvider,
) : ContainerRenderer, NavigationContainer.Component {

    private val fragmentManager = context.fragmentManager

    override var isVisible: Boolean
        get() {
            val view = when (context.contextReference) {
                is Activity -> context.contextReference.findViewById<View>(containerId)
                is Fragment -> context.contextReference.view?.findViewById<View>(containerId)
                else -> null
            } ?: return  false

            return view.isVisible
        }
        set(value) {
            val view = when (context.contextReference) {
                is Activity -> context.contextReference.findViewById<View>(containerId)
                is Fragment -> context.contextReference.view?.findViewById<View>(containerId)
                else -> null
            } ?: return

            view.isVisible = value
        }

    private var renderJob: Job? = null
    private var lastRenderedBackstack: NavigationBackstack = emptyBackstack()

    override fun create(state: ContainerState) {
        if (renderJob != null) error("FragmentContainerRenderer is already bound")
        renderJob = context.lifecycleOwner.lifecycleScope.launch {
            context.lifecycle.withCreated {}
            state.backstackFlow.collectLatest {
                val transition = NavigationBackstackTransition(lastRenderedBackstack to it)
                while (!onBackstackUpdated(transition) && isActive) {
                    delay(16)
                }
                Log.e("Rendered", "${transition.activeBackstack.joinToString { it.navigationKey::class.java.simpleName }}")
                lastRenderedBackstack = it
            }
        }
    }

    override fun destroy() {
        renderJob?.cancel()
        renderJob = null
    }

    private fun onBackstackUpdated(
        transition: NavigationBackstackTransition
    ): Boolean {
        if (!tryExecutePendingTransitions()) return false
        if (fragmentManager.isStateSaved) return false
        val backstack = transition.activeBackstack

        val activePushed = getActivePushedInstruction(backstack)
            .withFragment(contextProvider::getOrCreateContext)
        if (activePushed != null
            && !activePushed.fragment.isAdded
            && activePushed.fragment.view != null
        ) {
            val hasAnimation = activePushed.fragment.view?.animation != null
            activePushed.fragment.view?.clearAnimation()
            if (hasAnimation) return false
        }

        val toPresent = getInstructionsToPresent(backstack)
            .mapNotNull { it.withFragment(contextProvider::getOrCreateContext) }

        val toDetach = getInstructionsToDetach(backstack)
            .mapNotNull { it.withFragment(contextProvider::getContext) }

        val toRemove = getFragmentsToRemove(backstack)
        val toRemoveDialogs = toRemove.filterIsInstance<DialogFragment>()
        val toRemoveDirect = toRemove.filter { it !is DialogFragment }

        (toDetach + activePushed)
            .filterNotNull()
            .forEach {
                setZIndexForAnimations(backstack, it.fragment)
            }

        fragmentManager.commitNow {
            applyAnimationsForTransaction(
                active = activePushed?.instruction
            )
            toRemoveDirect.forEach {
                remove(it)
                contextProvider.ownedFragments.remove(it.tag)
            }
            runOnCommit {
                toRemoveDialogs.forEach {
                    it.dismiss()
                    contextProvider.ownedFragments.remove(it.tag)
                }
            }
            toDetach.forEach {
                detach(it.fragment)
            }
            if (activePushed != null) {
                when {
                    activePushed.fragment.id != 0 -> attach(activePushed.fragment)
                    else -> add(
                        containerId,
                        activePushed.fragment,
                        activePushed.instruction.instructionId
                    )
                }
            }
            toPresent.forEach {
                applyAnimationsForTransaction(
                    active = activePushed?.instruction
                )
                if (it.fragment is DialogFragment) {
                    if (it.fragment.isAdded) {
                    } else if (it.fragment.isDetached) {
                        attach(it.fragment)
                    } else {
                        add(it.fragment, it.instruction.instructionId)
                    }
                } else {
                    if (it.fragment.id != 0) {
                        attach(it.fragment)
                    } else {
                        add(containerId, it.fragment, it.instruction.instructionId)
                    }
                }
            }
            val active = toPresent.lastOrNull()
                ?: activePushed
                ?: return@commitNow

            setPrimaryNavigationFragment(active.fragment)
        }

        backstack.lastOrNull()
            ?.let {
                fragmentManager.findFragmentByTag(it.instructionId)
            }
            ?.let { primaryFragment ->
                fragmentManager.commitNow {
                    setPrimaryNavigationFragment(primaryFragment)
                }
            }
        return true
    }

    private fun getInstructionsToDetach(backstackState: List<AnyOpenInstruction>): List<AnyOpenInstruction> {
        val pushed =
            backstackState.indexOfLast { it.navigationDirection == NavigationDirection.Push }

        val presented =
            backstackState.indexOfLast { it.navigationDirection == NavigationDirection.Present }
                .takeIf { it > pushed } ?: -1

        return backstackState
            .filterIndexed { i, _ ->
                i != pushed && i != presented
            }
    }

    private fun getFragmentsToRemove(backstackState: List<AnyOpenInstruction>): List<Fragment> {
        val activeIds = backstackState.map { it.instructionId }.toSet()
        contextProvider.ownedFragments.addAll(activeIds)
        return contextProvider.ownedFragments.filter { !activeIds.contains(it) }
            .mapNotNull { fragmentManager.findFragmentByTag(it) }
    }

    private fun getInstructionsToPresent(backstackState: List<AnyOpenInstruction>): List<AnyOpenInstruction> {
        return backstackState
            .takeLastWhile {
                it.navigationDirection is NavigationDirection.Present
            }
            .takeLast(1)
    }

    private fun getActivePushedInstruction(backstackState: List<AnyOpenInstruction>): AnyOpenInstruction? {
        return backstackState
            .lastOrNull {
                it.navigationDirection is NavigationDirection.Push
            }
    }

    // TODO this doesn't work, it needs to know about the exiting element
    private fun setZIndexForAnimations(
        backstack: List<AnyOpenInstruction>,
        fragment: Fragment
    ) {
        val activeIndex = backstack.indexOfFirst { it.instructionId == backstack.lastOrNull()?.instructionId }
        val index = backstack.indexOfFirst { it.instructionId == fragment.tag }

        fragment.view?.z = when {
            index == activeIndex -> 0f
            index < activeIndex -> -1f
            else -> 1f
        }
    }

    private fun FragmentTransaction.applyAnimationsForTransaction(
        active: AnyOpenInstruction?
    ) {
//        val previouslyActiveFragment = fragmentManager.findFragmentById(containerId)
//        val entering = (active?.let { getAnimationsForEntering(it.instruction) }
//            ?: DefaultAnimations.none.entering).asResource(context.activity.theme)
//        val exiting = (state.currentTransition.exitingInstruction?.let { getAnimationsForExiting(it) }
//            ?: DefaultAnimations.none.exiting).asResource(context.activity.theme)
//
//        val noOpEntering = when {
//            exiting.isAnimator(context.activity) -> R.animator.animator_example_no
//            else -> R.anim.enro_no_op_enter_animation
//        }
//
//        // When a FragmentTransaction uses custom animations that are of the same anim/animator type,
//        // the anim is disregarded, and the Fragment that would receive the anim does not receive any
//        // animation. So, what we're doing here is falling back to a default anim or animator resource
//        // for the exit animation, in the case that the enter/exit anim/animator types do not match.
//        val exitingId = when {
//            previouslyActiveFragment is NavigationHost -> when {
//                entering.isAnimator(context.activity) -> R.animator.animator_no_op_exit
//                else -> R.anim.enro_no_op_exit_animation
//            }
//
//            exiting.id == 0 -> 0
//            entering.isAnimator(context.activity)
//                    && !exiting.isAnimator(context.activity) -> {
//                Log.e(
//                    "Enro",
//                    "Fragment enter animation was 'animator' and exit was 'anim', falling back to default animator for exit animations"
//                )
//                R.animator.animator_enro_fallback_exit
//            }
//
//            entering.isAnim(context.activity)
//                    && !exiting.isAnim(context.activity) -> {
//                Log.e(
//                    "Enro",
//                    "Fragment enter animation was 'anim' and exit was 'animator', falling back to default anim for exit animations"
//                )
//                R.anim.enro_fallback_exit
//            }
//
//            else -> exiting.id
//        }
//
//        setCustomAnimations(
//            if (active?.fragment is NavigationHost) noOpEntering else entering.id,
//            exitingId
//        )
    }

    private fun tryExecutePendingTransitions(): Boolean {
        return kotlin
            .runCatching {
                fragmentManager.executePendingTransactions()
                true
            }
            .getOrDefault(false)
    }
}
