package dev.enro.core.controller.usecase

import dev.enro.animation.NavigationAnimation
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.container.NavigationBackstackTransition
import dev.enro.core.container.NavigationContainer
import dev.enro.core.controller.get
import kotlin.collections.set

// TODO: Merge this with the GetNavigationAnimations use case?
internal class GetAnimationsForTransition {
    fun getAnimations(
        container: NavigationContainer,
        transition: NavigationBackstackTransition,
    ): Map<String, NavigationAnimation> {
        val animations = mutableMapOf<String, NavigationAnimation>()
        val activeInstruction = transition.activeBackstack.active
        val exitingInstruction = transition.exitingInstruction

        (transition.removed + transition.activeBackstack).forEach { instruction ->
            val state = getAnimationState(transition, instruction)
            val animation = when (state) {
                AnimationState.Entering -> container.getNavigationAnimations.opening(
                    exiting = exitingInstruction,
                    entering = instruction,
                ).entering

                AnimationState.Exiting -> when (activeInstruction == null) {
                    true -> null
                    else -> container.getNavigationAnimations.opening(
                        exiting = instruction,
                        entering = activeInstruction,
                    ).exiting
                }

                AnimationState.ReturnEnter -> container.getNavigationAnimations.closing(
                    exiting = instruction,
                    entering = activeInstruction,
                ).entering

                AnimationState.ReturnExit -> container.getNavigationAnimations.closing(
                    exiting = instruction,
                    entering = activeInstruction,
                ).exiting


                AnimationState.None -> null
            }
            if (animation == null) return@forEach
            animations[instruction.instructionId] = animation
        }
        return animations
    }

    private fun NavigationContainer.getAnimationsForPredictiveBackExit(
        predictiveClosing: AnyOpenInstruction,
        predictiveActive: AnyOpenInstruction?,
    ): NavigationAnimation {
        val animations = dependencyScope.get<GetNavigationAnimations>()
        return animations.closing(predictiveClosing, predictiveActive).exiting
    }

    private fun NavigationContainer.getAnimationsForPredictiveBackEnter(
        predictiveClosing: AnyOpenInstruction,
        predictiveActive: AnyOpenInstruction,
    ): NavigationAnimation {
        val animations = dependencyScope.get<GetNavigationAnimations>()
        return animations.closing(predictiveClosing, predictiveActive).entering
    }

    private fun getAnimationState(
        transition: NavigationBackstackTransition,
        instruction: AnyOpenInstruction,
    ): AnimationState {
        val instructionId = instruction.instructionId

        val isActive = transition.activeBackstack.activePushed?.instructionId == instructionId
                || transition.activeBackstack.activePresented?.instructionId == instructionId

        val wasActive = transition.previousBackstack.activePushed?.instructionId == instructionId
                || transition.previousBackstack.activePresented?.instructionId == instructionId

        val isRemoved = transition.removed.any { it.instructionId == instructionId }
        val isReturning =
            isActive && transition.previousBackstack.any { it.instructionId == instructionId }

        // If the instruction is active now and was active previously, no animation is needed,
        // as we want to keep the previous animation
        if (isActive && wasActive) {
            return AnimationState.None
        }

        if (isActive) {
            return when (isReturning) {
                true -> AnimationState.ReturnEnter
                false -> AnimationState.Entering
            }
        }
        if (wasActive) {
            return when (isRemoved) {
                true -> AnimationState.ReturnExit
                false -> AnimationState.Exiting
            }
        }

        return AnimationState.None
    }

    enum class AnimationState {
        Entering,    // Instruction is becoming active/visible
        Exiting,     // Instruction is no longer active/visible
        ReturnEnter, // Instruction is returning to view (going back)
        ReturnExit,  // Instruction is exiting during a back navigation
        None,        // No animation needed
    }
}