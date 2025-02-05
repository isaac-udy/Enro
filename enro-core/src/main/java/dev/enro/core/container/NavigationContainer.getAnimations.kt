package dev.enro.core.container

import dev.enro.animation.DefaultAnimations
import dev.enro.animation.NavigationAnimation
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationHost
import dev.enro.core.NavigationInstruction
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.GetNavigationAnimations
import dev.enro.core.parentContainer


private fun NavigationContainer.getTransitionForInstruction(instruction: AnyOpenInstruction): NavigationBackstackTransition {
    val isHosted = context.contextReference is NavigationHost
    if (!isHosted) return currentTransition

    val parentContainer = context.parentContainer() ?: return currentTransition
    val parentRoot = parentContainer.currentTransition.activeBackstack.getOrNull(0)
    val parentActive = parentContainer.currentTransition.activeBackstack.active
    val thisRoot = currentTransition.activeBackstack.getOrNull(0)
    if (parentRoot == thisRoot && parentRoot == parentActive) {
        val mergedPreviousBackstack = merge(
            currentTransition.previousBackstack,
            parentContainer.currentTransition.previousBackstack
        ).toBackstack()

        val mergedActiveBackstack = merge(
            currentTransition.activeBackstack.orEmpty(),
            parentContainer.currentTransition.activeBackstack.orEmpty()
        ).toBackstack()

        return NavigationBackstackTransition(mergedPreviousBackstack to mergedActiveBackstack)
    }

    val isRootInstruction =
        backstack.size <= 1 || backstack.firstOrNull()?.instructionId == instruction.instructionId
    if (!isRootInstruction) return currentTransition

    val isLastInstruction = parentContainer.currentTransition.lastInstruction == instruction
    val isExitingInstruction =
        parentContainer.currentTransition.exitingInstruction?.instructionId == instruction.instructionId
    val isEnteringInstruction =
        parentContainer.currentTransition.activeBackstack.active?.instructionId == instruction.instructionId

    if (isLastInstruction ||
        isExitingInstruction ||
        isEnteringInstruction
    ) return parentContainer.currentTransition

    return currentTransition
}

public fun NavigationContainer.getAnimationsForEntering(instruction: AnyOpenInstruction): NavigationAnimation {
    val animations = dependencyScope.get<GetNavigationAnimations>()
    val currentTransition = getTransitionForInstruction(instruction)

    val isInitialInstruction =
        currentTransition.previousBackstack.identity == NavigationContainer.initialBackstack.identity
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

public fun NavigationContainer.getAnimationsForExiting(instruction: AnyOpenInstruction): NavigationAnimation {
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

public fun NavigationContainer.getAnimationsForPredictiveBackExit(
    predictiveClosing: AnyOpenInstruction,
    predictiveActive: AnyOpenInstruction?,
): NavigationAnimation {
    val animations = dependencyScope.get<GetNavigationAnimations>()
    return animations.closing(predictiveClosing, predictiveActive).exiting
}

public fun NavigationContainer.getAnimationsForPredictiveBackEnter(
    predictiveClosing: AnyOpenInstruction,
    predictiveActive: AnyOpenInstruction,
): NavigationAnimation {
    val animations = dependencyScope.get<GetNavigationAnimations>()
    return animations.closing(predictiveClosing, predictiveActive).entering
}