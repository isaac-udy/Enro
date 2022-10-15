package dev.enro.core.fragment.container

import android.app.Activity
import android.view.View
import androidx.annotation.IdRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commitNow
import dev.enro.core.*
import dev.enro.core.compose.ComposableNavigationBinding
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.NavigationBackstack
import dev.enro.core.container.NavigationContainer
import dev.enro.core.fragment.FragmentNavigationBinding
import dev.enro.core.hosts.AbstractFragmentHostForComposable
import dev.enro.extensions.animate

public class FragmentNavigationContainer internal constructor(
    @IdRes public val containerId: Int,
    parentContext: NavigationContext<*>,
    accept: (NavigationKey) -> Boolean,
    emptyBehavior: EmptyBehavior,
    initialBackstack: NavigationBackstack
) : NavigationContainer(
    id = containerId.toString(),
    parentContext = parentContext,
    acceptsNavigationKey = accept,
    emptyBehavior = emptyBehavior,
    acceptsDirection = { it is NavigationDirection.Push || it is NavigationDirection.Forward },
    acceptsBinding = { it is FragmentNavigationBinding<*, *> || it is ComposableNavigationBinding<*, *> }
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
        setOrLoadInitialBackstack(initialBackstack)
    }

    override fun reconcileBackstack(
        removed: List<AnyOpenInstruction>,
        backstack: NavigationBackstack
    ): Boolean {
        if (!tryExecutePendingTransitions()) return false
        if (fragmentManager.isStateSaved) return false
        if (backstack != backstackFlow.value) return false

        val toRemove = removed.asFragmentAndInstruction()
        val toDetach = getFragmentsToDetach(backstack)
        val active = getOrCreateActiveFragment(backstack)

        (toRemove + toDetach + active)
            .filterNotNull()
            .forEach {
                setZIndexForAnimations(backstack, it)
            }

        fragmentManager.commitNow {
            setReorderingAllowed(true)
            setAnimationsForTransaction(
                backstack = backstack,
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

    private fun getFragmentsToDetach(backstack: NavigationBackstack): List<FragmentAndInstruction> {
        return backstack.backstack
            .filter { it.navigationDirection !is NavigationDirection.Present }
            .filter { it != backstack.active }
            .asFragmentAndInstruction()
    }

    private fun getOrCreateActiveFragment(backstack: NavigationBackstack): FragmentAndInstruction? {
        backstack.active ?: return null
        val existingFragment = fragmentManager.findFragmentByTag(backstack.active.instructionId)
        if (existingFragment != null) return FragmentAndInstruction(
            fragment = existingFragment,
            instruction = backstack.active
        )

        val binding =
            parentContext.controller.bindingForKeyType(backstack.active.navigationKey::class)
                ?: throw EnroException.UnreachableState()

        return FragmentAndInstruction(
            fragment = FragmentFactory.createFragment(
                parentContext,
                binding,
                backstack.active
            ),
            instruction = backstack.active
        )
    }

    private fun setZIndexForAnimations(backstack: NavigationBackstack, fragmentAndInstruction: FragmentAndInstruction) {
        val activeIndex = backstack.renderable.indexOfFirst { it.instructionId == backstack.active?.instructionId }
        val index = backstack.renderable.indexOf(fragmentAndInstruction.instruction)

        fragmentAndInstruction.fragment.view?.z = when {
            index == activeIndex -> 0f
            index < activeIndex -> -1f
            else -> 1f
        }
    }

    private fun FragmentTransaction.setAnimationsForTransaction(
        backstack: NavigationBackstack,
        active: FragmentAndInstruction?
    ) {
        if (backstack.isDirectUpdate) return
        val previouslyActiveFragment = fragmentManager.findFragmentById(containerId)
        currentAnimations =
            animationsFor(previouslyActiveFragment?.navigationContext ?: parentContext, backstack.lastInstruction)
        val resourceAnimations = currentAnimations.asResource(parentContext.activity.theme)

        setCustomAnimations(
            if (active?.fragment is AbstractFragmentHostForComposable) R.anim.enro_no_op_enter_animation else resourceAnimations.enter,
            if (previouslyActiveFragment is AbstractFragmentHostForComposable) R.anim.enro_no_op_exit_animation else resourceAnimations.exit
        )
    }
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
