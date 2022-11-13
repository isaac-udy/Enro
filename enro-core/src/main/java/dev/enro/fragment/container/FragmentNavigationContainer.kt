package dev.enro.fragment.container

import android.app.Activity
import android.view.View
import androidx.annotation.IdRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commitNow
import dev.enro.core.*
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationBackstackState
import dev.enro.core.container.NavigationContainer
import dev.enro.core.interceptor.builder.NavigationInterceptorBuilder
import dev.enro.extensions.animate
import dev.enro.fragment.FragmentContext

public class FragmentNavigationContainer internal constructor(
    @IdRes public val containerId: Int,
    parentContext: NavigationContext<*>,
    accept: (NavigationKey) -> Boolean,
    emptyBehavior: EmptyBehavior,
    interceptor: NavigationInterceptorBuilder.() -> Unit,
    initialBackstackState: NavigationBackstackState,
) : NavigationContainer(
    id = containerId.toString(),
    contextType = Fragment::class.java,
    parentContext = parentContext,
    acceptsNavigationKey = accept,
    emptyBehavior = emptyBehavior,
    interceptor = interceptor,
    acceptsDirection = { it is NavigationDirection.Push || it is NavigationDirection.Forward },
    navigationHostFactory = parentContext.controller.dependencyScope.get()
) {
    override var isVisible: Boolean
        get() {
            return containerView?.isVisible ?: false
        }
        set(value) {
            containerView?.isVisible = value
        }

    override val activeContext: NavigationContext<out Fragment>?
        get() = fragmentManager.findFragmentById(containerId)?.navigationContext

    override var currentAnimations: NavigationAnimation = DefaultAnimations.none
        private set

    init {
        setOrLoadInitialBackstack(initialBackstackState)
    }

    override fun reconcileBackstack(
        removed: List<AnyOpenInstruction>,
        backstackState: NavigationBackstackState
    ): Boolean {
        if (!tryExecutePendingTransitions()) return false
        if (fragmentManager.isStateSaved) return false
        if (backstackState != backstackFlow.value) return false

        val toRemove = removed.asFragmentAndInstruction()
        val toDetach = getFragmentsToDetach(backstackState)
        val active = getOrCreateActiveFragment(backstackState)

        (toRemove + toDetach + active)
            .filterNotNull()
            .forEach {
                setZIndexForAnimations(backstackState, it)
            }

        setAnimations(backstackState)
        fragmentManager.commitNow {
            setReorderingAllowed(true)
            applyAnimationsForTransaction(
                backstackState = backstackState,
                active = active
            )

            toRemove.forEach { remove(it.fragment) }
            toDetach.forEach { detach(it.fragment) }
            if (active == null) return@commitNow

            when {
                active.fragment.id != 0 -> attach(active.fragment)
                else -> add(
                    containerId,
                    active.fragment,
                    active.instruction.instructionId
                )
            }
            setPrimaryNavigationFragment(active.fragment)
        }
        return true
    }

    private fun List<AnyOpenInstruction>.asFragmentAndInstruction(): List<FragmentAndInstruction> {
        return mapNotNull { instruction ->
            val fragment = fragmentManager.findFragmentByTag(instruction.instructionId) ?: return@mapNotNull null
            FragmentAndInstruction(
                fragment = fragment,
                instruction = instruction
            )
        }
    }

    private fun getFragmentsToDetach(backstackState: NavigationBackstackState): List<FragmentAndInstruction> {
        return backstackState.backstack
            .filter { it.navigationDirection !is NavigationDirection.Present }
            .filter { it != backstackState.active }
            .asFragmentAndInstruction()
    }

    private fun getOrCreateActiveFragment(backstackState: NavigationBackstackState): FragmentAndInstruction? {
        backstackState.active ?: return null
        val existingFragment = fragmentManager.findFragmentByTag(backstackState.active.instructionId)
        if (existingFragment != null) return FragmentAndInstruction(
            fragment = existingFragment,
            instruction = backstackState.active
        )

        val binding =
            parentContext.controller.bindingForKeyType(backstackState.active.navigationKey::class)
                ?: throw EnroException.UnreachableState()

        return FragmentAndInstruction(
            fragment = FragmentFactory.createFragment(
                parentContext,
                backstackState.active
            ),
            instruction = backstackState.active
        )
    }

    private fun setZIndexForAnimations(backstack: NavigationBackstackState, fragmentAndInstruction: FragmentAndInstruction) {
        val activeIndex =
            backstack.renderable.indexOfFirst { it.instructionId == backstack.active?.instructionId }
        val index = backstack.renderable.indexOf(fragmentAndInstruction.instruction)

        fragmentAndInstruction.fragment.view?.z = when {
            index == activeIndex -> 0f
            index < activeIndex -> -1f
            else -> 1f
        }
    }

    private fun setAnimations(backstackState: NavigationBackstackState) {
        val shouldTakeAnimationsFromParentContainer = parentContext is FragmentContext<out Fragment>
                && parentContext.contextReference is ProvidesInitialAnimationsForChildren
                && backstackState.backstack.size <= 1

        val previouslyActiveFragment = fragmentManager.findFragmentById(containerId)
        val previouslyActiveContext = runCatching { previouslyActiveFragment?.navigationContext }.getOrNull()
        currentAnimations = when {
            backstackState.isRestoredState -> DefaultAnimations.none
            shouldTakeAnimationsFromParentContainer -> parentContext.parentContainer()!!.currentAnimations
            backstackState.isInitialState -> DefaultAnimations.none
            else -> animationsFor(
                previouslyActiveContext ?: parentContext,
                backstackState.lastInstruction
            )
        }
    }

    private fun FragmentTransaction.applyAnimationsForTransaction(
        backstackState: NavigationBackstackState,
        active: FragmentAndInstruction?
    ) {
        if (backstackState.isRestoredState) return
        val previouslyActiveFragment = fragmentManager.findFragmentById(containerId)
        val resourceAnimations = currentAnimations.asResource(parentContext.activity.theme)

        setCustomAnimations(
            if (active?.fragment is IgnoresEnterAnimation) R.anim.enro_no_op_enter_animation else resourceAnimations.enter,
            if (previouslyActiveFragment is IgnoresExitAnimation) R.anim.enro_no_op_exit_animation else resourceAnimations.exit
        )
    }

    internal interface IgnoresEnterAnimation
    internal interface IgnoresExitAnimation
    internal interface ProvidesInitialAnimationsForChildren
}

private data class FragmentAndInstruction(
    val fragment: Fragment,
    val instruction: AnyOpenInstruction
)

public val FragmentNavigationContainer.containerView: View?
    get() {
        return when (parentContext.contextReference) {
            is Activity -> parentContext.contextReference.findViewById(containerId)
            is Fragment -> parentContext.contextReference.view?.findViewById(containerId)
            else -> null
        }
    }

public fun FragmentNavigationContainer.setVisibilityAnimated(
    isVisible: Boolean,
    animations: NavigationAnimation = DefaultAnimations.present
) {
    val view = containerView ?: return
    if (!view.isVisible && !isVisible) return
    if (view.isVisible && isVisible) return

    val resourceAnimations = animations.asResource(view.context.theme)
    view.animate(
        animOrAnimator = when (isVisible) {
            true -> resourceAnimations.enter
            false -> resourceAnimations.exit
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
