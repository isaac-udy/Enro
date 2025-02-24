package dev.enro.core.fragment.container

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.IdRes
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commitNow
import androidx.lifecycle.lifecycleScope
import dev.enro.animation.DefaultAnimations
import dev.enro.animation.NavigationAnimationOverrideBuilder
import dev.enro.animation.NavigationAnimationTransition
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContainerKey
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationHost
import dev.enro.core.R
import dev.enro.core.activity
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationBackstack
import dev.enro.core.container.NavigationBackstackTransition
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.NavigationContainerBackEvent
import dev.enro.core.container.NavigationInstructionFilter
import dev.enro.core.container.close
import dev.enro.core.container.getAnimationsForEntering
import dev.enro.core.container.getAnimationsForExiting
import dev.enro.core.controller.get
import dev.enro.core.controller.interceptor.builder.NavigationInterceptorBuilder
import dev.enro.core.controller.usecase.HostInstructionAs
import dev.enro.core.getNavigationHandle
import dev.enro.core.navigationContext
import dev.enro.core.requestClose
import dev.enro.destination.fragment.FragmentSharedElements
import dev.enro.extensions.animate
import dev.enro.extensions.getParcelableCompat
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

public class FragmentNavigationContainer internal constructor(
    @IdRes public val containerId: Int,
    key: NavigationContainerKey = NavigationContainerKey.FromId(containerId),
    parentContext: NavigationContext<*>,
    filter: NavigationInstructionFilter,
    emptyBehavior: EmptyBehavior,
    interceptor: NavigationInterceptorBuilder.() -> Unit,
    animations: NavigationAnimationOverrideBuilder.() -> Unit,
    initialBackstack: NavigationBackstack,
) : NavigationContainer(
    key = key,
    context = parentContext,
    contextType = Fragment::class.java,
    instructionFilter = filter,
    emptyBehavior = emptyBehavior,
    interceptor = interceptor,
    animations = animations,
) {
    private val hostInstructionAs =
        parentContext.controller.dependencyScope.get<HostInstructionAs>()

    override var isVisible: Boolean
        get() {
            return containerView?.isVisible ?: false
        }
        set(value) {
            containerView?.isVisible = value
        }

    private val ownedFragments = mutableSetOf<String>()
    private val restoredFragmentStates = mutableMapOf<String, Fragment.SavedState>()

    init {
        backEvents
            .onEach { backEvent ->
                if (backEvent is NavigationContainerBackEvent.Confirmed) {
                    backEvent.context.getNavigationHandle().requestClose()
                }
            }
            .launchIn(context.lifecycleOwner.lifecycleScope)

        fragmentManager.registerFragmentLifecycleCallbacks(object : FragmentLifecycleCallbacks() {
            override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
                if (f !is DialogFragment) return
                val instructionId = f.tag ?: return
                if (fm.isDestroyed || fm.isStateSaved) return
                if (!f.isRemoving) return
                ownedFragments.remove(f.tag)
                setBackstack(backstack.close(instructionId))
            }
        }, false)

        restoreOrSetBackstack(initialBackstack)
    }

    public override fun save(): Bundle {
        val savedState = super.save()
        backstack.asFragmentAndInstruction()
            .forEach {
                val fragmentState = fragmentManager.saveFragmentInstanceState(it.fragment)
                savedState.putParcelable(
                    "${FRAGMENT_STATE_PREFIX_KEY}${it.instruction.instructionId}",
                    fragmentState
                )
            }
        savedState.putStringArrayList(OWNED_FRAGMENTS_KEY, ArrayList(ownedFragments))
        return savedState
    }

    public override fun restore(bundle: Bundle) {
        bundle.keySet().forEach { key ->
            if (!key.startsWith(FRAGMENT_STATE_PREFIX_KEY)) return@forEach
            val fragmentState =
                bundle.getParcelableCompat<Fragment.SavedState>(key) ?: return@forEach
            val instructionId = key.removePrefix(FRAGMENT_STATE_PREFIX_KEY)
            restoredFragmentStates[instructionId] = fragmentState
        }
        ownedFragments.addAll(bundle.getStringArrayList(OWNED_FRAGMENTS_KEY).orEmpty())
        super.restore(bundle)

        // After the backstack has been set, we're going to remove the restored states which aren't in the backstack
        val instructionsInBackstack = backstack.map { it.instructionId }.toSet()
        restoredFragmentStates.keys.minus(instructionsInBackstack).forEach {
            restoredFragmentStates.remove(it)
        }
    }

    override fun getChildContext(contextFilter: ContextFilter): NavigationContext<*>? {
        val fragment = when(contextFilter) {
            is ContextFilter.Active -> {
                backstack.active
                    ?.let { fragmentManager.findFragmentByTag(it.instructionId) }
                    ?: fragmentManager.findFragmentById(containerId)
            }
            is ContextFilter.ActivePushed -> {
                backstack.activePushed
                    ?.let { fragmentManager.findFragmentByTag(it.instructionId) }
            }
            is ContextFilter.ActivePresented -> {
                backstack.activePresented
                    ?.let { fragmentManager.findFragmentByTag(it.instructionId) }
            }
            is ContextFilter.WithId -> {
                fragmentManager.findFragmentByTag(contextFilter.id)
            }
        }
        return fragment?.navigationContext
    }

    override fun onBackstackUpdated(
        transition: NavigationBackstackTransition
    ): Boolean {
        if (!tryExecutePendingTransitions()) return false
        if (fragmentManager.isStateSaved) return false

        val activePushed = getActivePushedFragment(backstack)
        if (activePushed != null
            && !activePushed.fragment.isAdded
            && activePushed.fragment.view != null
        ) {
            val hasAnimation = activePushed.fragment.view?.animation != null
            activePushed.fragment.view?.clearAnimation()
            if (hasAnimation) return false
        }

        val toPresent = getFragmentsToPresent(backstack)
        val toDetach = getFragmentsToDetach(backstack)
        val toRemove = getFragmentsToRemove(backstack)
        val toRemoveDialogs = toRemove.filterIsInstance<DialogFragment>()
        val toRemoveDirect = toRemove.filter { it !is DialogFragment }

        (toDetach + activePushed)
            .filterNotNull()
            .forEach {
                setZIndexForAnimations(backstack, it)
            }

        fragmentManager.commitNow {
            applyAnimationsForTransaction(
                active = activePushed
            )
            toRemoveDirect.forEach {
                remove(it)
                FragmentSharedElements.getSharedElements(it).forEach { sharedElement ->
                    addSharedElement(sharedElement.view, sharedElement.name)
                }
                ownedFragments.remove(it.tag)
            }
            runOnCommit {
                toRemoveDialogs.forEach {
                    it.dismiss()
                    ownedFragments.remove(it.tag)
                }
            }
            toDetach.forEach {
                FragmentSharedElements.getSharedElements(it.fragment).forEach { sharedElement ->
                    addSharedElement(sharedElement.view, sharedElement.name)
                }
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
                    active = it
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
            val activeFragmentAndInstruction = toPresent.lastOrNull()
                ?: activePushed
                ?: return@commitNow

            val activeFragment = activeFragmentAndInstruction.fragment
            setPrimaryNavigationFragment(activeFragment)
        }

        backstack.lastOrNull()
            ?.let {
                fragmentManager.findFragmentByTag(it.instructionId)
            }
            ?.let { primaryFragment ->
                if (fragmentManager.primaryNavigationFragment != primaryFragment) {
                    fragmentManager.commitNow {
                        setPrimaryNavigationFragment(primaryFragment)
                    }
                }
            }
        return true
    }

    private fun List<AnyOpenInstruction>.asFragmentAndInstruction(): List<FragmentAndInstruction> {
        return mapNotNull { instruction ->
            val fragment = fragmentManager.findFragmentByTag(instruction.instructionId)
                ?: return@mapNotNull null
            FragmentAndInstruction(
                fragment = fragment,
                instruction = instruction
            )
        }
    }

    private fun getFragmentsToDetach(backstackState: List<AnyOpenInstruction>): List<FragmentAndInstruction> {
        val pushed =
            backstackState.indexOfLast { it.navigationDirection == NavigationDirection.Push }

        val presented =
            backstackState.indexOfLast { it.navigationDirection == NavigationDirection.Present }
                .takeIf { it > pushed } ?: -1

        return backstackState
            .filterIndexed { i, _ ->
                i != pushed && i != presented
            }
            .asFragmentAndInstruction()
    }

    private fun getFragmentsToRemove(backstackState: List<AnyOpenInstruction>): List<Fragment> {
        val activeIds = backstackState.map { it.instructionId }.toSet()
        ownedFragments.addAll(activeIds)
        return ownedFragments.filter { !activeIds.contains(it) }
            .mapNotNull { fragmentManager.findFragmentByTag(it) }
    }

    private fun getFragmentsToPresent(backstackState: List<AnyOpenInstruction>): List<FragmentAndInstruction> {
        return backstackState
            .takeLastWhile {
                it.navigationDirection is NavigationDirection.Present
            }
            .map {
                val cls = when (containerId) {
                    android.R.id.content -> DialogFragment::class.java
                    else -> Fragment::class.java
                }
                getOrCreateFragment(cls, it)
            }
            .takeLast(1)
    }

    private fun getActivePushedFragment(backstackState: List<AnyOpenInstruction>): FragmentAndInstruction? {
        val activePushedFragment = backstackState
            .lastOrNull {
                it.navigationDirection is NavigationDirection.Push
            } ?: return null
        return getOrCreateFragment(Fragment::class.java, activePushedFragment)
    }

    private fun getOrCreateFragment(
        type: Class<out Fragment>,
        instruction: AnyOpenInstruction
    ): FragmentAndInstruction {
        val existingFragment = fragmentManager.findFragmentByTag(instruction.instructionId)
        if (existingFragment != null) return FragmentAndInstruction(
            fragment = existingFragment,
            instruction = instruction
        )

        val fragment = FragmentFactory.createFragment(
            parentContext = context,
            instruction = hostInstructionAs(type, context, instruction)
        )

        val restoredState = restoredFragmentStates.remove(instruction.instructionId)
        if (restoredState != null) fragment.setInitialSavedState(restoredState)

        return FragmentAndInstruction(
            fragment = fragment,
            instruction = instruction
        )
    }

    // TODO this doesn't work, it needs to know about the exiting element
    private fun setZIndexForAnimations(
        backstack: List<AnyOpenInstruction>,
        fragmentAndInstruction: FragmentAndInstruction
    ) {
        val activeIndex =
            backstack.indexOfFirst { it.instructionId == backstack.lastOrNull()?.instructionId }
        val index = backstack.indexOf(fragmentAndInstruction.instruction)

        fragmentAndInstruction.fragment.view?.z = when {
            index == activeIndex -> 0f
            index < activeIndex -> -1f
            else -> 1f
        }
    }

    private fun FragmentTransaction.applyAnimationsForTransaction(
        active: FragmentAndInstruction?
    ) {
        val previouslyActiveFragment = fragmentManager.findFragmentById(containerId)
        val entering = (active?.let { getAnimationsForEntering(it.instruction) }
            ?: DefaultAnimations.none.entering).asResource(context.activity.theme)
        val exiting = (currentTransition.exitingInstruction?.let { getAnimationsForExiting(it) }
            ?: DefaultAnimations.none.exiting).asResource(context.activity.theme)

        val noOpEntering = when {
            exiting.isAnimator(context.activity) -> R.animator.animator_example_no
            else -> R.anim.enro_no_op_enter_animation
        }

        // When a FragmentTransaction uses custom animations that are of the same anim/animator type,
        // the anim is disregarded, and the Fragment that would receive the anim does not receive any
        // animation. So, what we're doing here is falling back to a default anim or animator resource
        // for the exit animation, in the case that the enter/exit anim/animator types do not match.
        val exitingId = when {
            previouslyActiveFragment is NavigationHost -> when {
                entering.isAnimator(context.activity) -> R.animator.animator_no_op_exit
                else -> R.anim.enro_no_op_exit_animation
            }

            exiting.id == 0 -> 0
            entering.isAnimator(context.activity)
                    && !exiting.isAnimator(context.activity) -> {
                Log.e(
                    "Enro",
                    "Fragment enter animation was 'animator' and exit was 'anim', falling back to default animator for exit animations"
                )
                R.animator.animator_enro_fallback_exit
            }

            entering.isAnim(context.activity)
                    && !exiting.isAnim(context.activity) -> {
                Log.e(
                    "Enro",
                    "Fragment enter animation was 'anim' and exit was 'animator', falling back to default anim for exit animations"
                )
                R.anim.enro_fallback_exit
            }

            else -> exiting.id
        }

        setCustomAnimations(
            if (active?.fragment is NavigationHost) noOpEntering else entering.id,
            exitingId
        )
    }

    private companion object {
        private const val FRAGMENT_STATE_PREFIX_KEY = "FragmentState@"
        private const val OWNED_FRAGMENTS_KEY = "OWNED_FRAGMENTS_KEY"
    }
}

private data class FragmentAndInstruction(
    val fragment: Fragment,
    val instruction: AnyOpenInstruction
)

public val FragmentNavigationContainer.containerView: View?
    get() {
        return when (context.contextReference) {
            is Activity -> context.contextReference.findViewById(containerId)
            is Fragment -> context.contextReference.view?.findViewById(containerId)
            else -> null
        }
    }

public fun FragmentNavigationContainer.setVisibilityAnimated(
    isVisible: Boolean,
    animations: NavigationAnimationTransition = NavigationAnimationTransition(
        entering = DefaultAnimations.ForView.presentEnter,
        exiting = DefaultAnimations.ForView.presentCloseExit,
    )
) {
    val view = containerView ?: return
    if (!view.isVisible && !isVisible) return
    if (view.isVisible && isVisible) return

    view.animate(
        animOrAnimator = when (isVisible) {
            true -> animations.entering.asResource(view.context.theme).id
            false -> animations.exiting.asResource(view.context.theme).id
        },
        onAnimationStart = {
            view.translationZ = if (isVisible) 0f else -1f
            view.isVisible = true
        },
        onAnimationEnd = {
            view.isVisible = isVisible
        }
    )
}
