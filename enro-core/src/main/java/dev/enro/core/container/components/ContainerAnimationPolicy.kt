package dev.enro.core.container.components

import dev.enro.animation.DefaultAnimations
import dev.enro.animation.NavigationAnimation
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationHost
import dev.enro.core.NavigationInstruction
import dev.enro.core.container.NavigationBackstackTransition
import dev.enro.core.container.NavigationContainer
import dev.enro.core.container.merge
import dev.enro.core.container.toBackstack
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.GetNavigationAnimations
import dev.enro.core.parentContainer

public interface ContainerAnimationPolicy {

    public fun getAnimationsForEntering(container: NavigationContainer, instruction: AnyOpenInstruction): NavigationAnimation
    public fun getAnimationsForExiting(container: NavigationContainer, instruction: AnyOpenInstruction): NavigationAnimation

    public class Default : ContainerAnimationPolicy {
        // When we've got a NavigationHost wrapping this ComposableNavigationContainer,
        // we want to take the animations provided by the NavigationHost's NavigationContainer,
        // and sometimes skip other animation jobs
        private val shouldTakeAnimationsFromParentContainer: Boolean
            get() = false
//        context.contextReference is NavigationHost
//                    && backstack.size <= 1
//                    && currentTransition.lastInstruction != NavigationInstruction.Close

        override fun getAnimationsForEntering(
            container: NavigationContainer,
            instruction: AnyOpenInstruction
        ): NavigationAnimation = container.getAnimationsForEntering(instruction)

        override fun getAnimationsForExiting(
            container: NavigationContainer,
            instruction: AnyOpenInstruction
        ): NavigationAnimation = container.getAnimationsForExiting(instruction)
    }
}

private fun NavigationContainer.getTransitionForInstruction(instruction: AnyOpenInstruction): NavigationBackstackTransition {
    val isHosted = context.contextReference is NavigationHost
    if (!isHosted) return state.currentTransition

    val parentContainer = context.parentContainer() ?: return state.currentTransition
    val parentRoot = parentContainer.state.currentTransition.activeBackstack.getOrNull(0)
    val parentActive = parentContainer.state.currentTransition.activeBackstack.active
    val thisRoot = state.currentTransition.activeBackstack.getOrNull(0)
    if (parentRoot == thisRoot && parentRoot == parentActive) {
        val mergedPreviousBackstack = merge(
            state.currentTransition.previousBackstack,
            parentContainer.state.currentTransition.previousBackstack
        ).toBackstack()

        val mergedActiveBackstack = merge(
            state.currentTransition.activeBackstack.orEmpty(),
            parentContainer.state.currentTransition.activeBackstack.orEmpty()
        ).toBackstack()

        return NavigationBackstackTransition(mergedPreviousBackstack to mergedActiveBackstack)
    }

    val isRootInstruction =
        backstack.size <= 1 || backstack.firstOrNull()?.instructionId == instruction.instructionId
    if (!isRootInstruction) return state.currentTransition

    val isLastInstruction = parentContainer.state.currentTransition.lastInstruction == instruction
    val isExitingInstruction =
        parentContainer.state.currentTransition.exitingInstruction?.instructionId == instruction.instructionId
    val isEnteringInstruction =
        parentContainer.state.currentTransition.activeBackstack.active?.instructionId == instruction.instructionId

    if (isLastInstruction ||
        isExitingInstruction ||
        isEnteringInstruction
    ) return parentContainer.state.currentTransition

    return state.currentTransition
}

private fun NavigationContainer.getAnimationsForEntering(instruction: AnyOpenInstruction): NavigationAnimation {
    val animations = dependencyScope.get<GetNavigationAnimations>()
    val currentTransition = getTransitionForInstruction(instruction)

    val isInitialInstruction =
        currentTransition.previousBackstack.identity == ContainerState.initialBackstack.identity
    if (isInitialInstruction) {
        return DefaultAnimations.noOp.entering
    }

    val exitingInstruction = currentTransition.exitingInstruction
        ?: return animations.opening(null, instruction).entering

    if (currentTransition.lastInstruction is NavigationInstruction.Close) {
        return animations.closing(exitingInstruction, instruction).entering
    }
    return animations.opening(exitingInstruction, instruction).entering
}

private fun NavigationContainer.getAnimationsForExiting(instruction: AnyOpenInstruction): NavigationAnimation {
    val animations = dependencyScope.get<GetNavigationAnimations>()
    val currentTransition = getTransitionForInstruction(instruction)

    val activeInstruction = currentTransition.activeBackstack.active
        ?: return animations.closing(instruction, null).exiting

    val closingNonActiveInstruction = !currentTransition.activeBackstack.contains(instruction)
            && currentTransition.previousBackstack.contains(instruction)
            && currentTransition.previousBackstack.indexOf(instruction) < currentTransition.previousBackstack.lastIndex

    if (
        currentTransition.lastInstruction is NavigationInstruction.Close ||
        backstack.isEmpty() ||
        (!currentTransition.activeBackstack.contains(instruction) && !closingNonActiveInstruction)
    ) {
        return animations.closing(instruction, activeInstruction).exiting
    }
    return animations.opening(instruction, activeInstruction).exiting
}