package dev.enro.core.controller.usecase

import dev.enro.animation.NavigationAnimation
import dev.enro.animation.findDefaults
import dev.enro.animation.findOverrideForClosing
import dev.enro.animation.findOverrideForOpening
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationDirection
import dev.enro.core.container.NavigationBackstackTransition
import dev.enro.core.container.NavigationContainer
import kotlin.collections.set
import kotlin.reflect.KClass

internal class GetAnimationsForTransition {

    inline fun <reified T: NavigationAnimation> getAnimations(
        container: NavigationContainer,
        transition: NavigationBackstackTransition,
    ): Map<String, T> {
        return getAnimations(
            type = T::class,
            container = container,
            transition = transition
        )
    }

    fun <T: NavigationAnimation> getAnimations(
        type: KClass<T>,
        container: NavigationContainer,
        transition: NavigationBackstackTransition,
    ): Map<String, T> {
        val animations = mutableMapOf<String, T>()
        val activeInstruction = transition.activeBackstack.active
        val exitingInstruction = transition.exitingInstruction

        val defaults = container.animationOverride.findDefaults(type)

        (transition.removed + transition.activeBackstack).forEach { instruction ->
            val state = getAnimationState(transition, instruction)
            if (state == AnimationState.None) return@forEach
            val defaultAnimation = getDefaultAnimationFor(defaults, state, instruction)
            animations[instruction.instructionId] = when (state) {
                AnimationState.Entering -> {
                    container.animationOverride.findOverrideForOpening(
                        type = type,
                        exiting = exitingInstruction,
                        entering = instruction,
                    ) ?: defaultAnimation
                }

                AnimationState.Exiting -> when (activeInstruction == null) {
                    true -> defaultAnimation
                    else -> container.animationOverride.findOverrideForOpening(
                        type = type,
                        exiting = instruction,
                        entering = activeInstruction,
                    ) ?: defaultAnimation
                }

                AnimationState.ReturnEnter -> container.animationOverride.findOverrideForClosing(
                    type = type,
                    exiting = instruction,
                    entering = activeInstruction,
                ) ?: defaultAnimation

                AnimationState.ReturnExit -> container.animationOverride.findOverrideForClosing(
                    type = type,
                    exiting = instruction,
                    entering = activeInstruction,
                ) ?: defaultAnimation

                AnimationState.None -> error("AnimationState.None should have already been filtered out")
            }
        }
        return animations
    }

    private fun <T : NavigationAnimation> NavigationContainer.getAnimationsForPredictiveBackExit(
        type: KClass<T>,
        predictiveClosing: AnyOpenInstruction,
        predictiveActive: AnyOpenInstruction?,
    ): NavigationAnimation {
        val defaults = animationOverride.findDefaults(type)
        return animationOverride.findOverrideForClosing(
            type = type,
            exiting = predictiveClosing,
            entering = predictiveActive,
        ) ?: when(predictiveClosing.navigationDirection) {
            NavigationDirection.Present -> defaults.presentReturn
            NavigationDirection.Push -> defaults.pushReturn
        }
    }

    private fun <T: NavigationAnimation> NavigationContainer.getAnimationsForPredictiveBackEnter(
        type: KClass<T>,
        predictiveClosing: AnyOpenInstruction,
        predictiveActive: AnyOpenInstruction,
    ): NavigationAnimation {
        val defaults = animationOverride.findDefaults(type)
        return animationOverride.findOverrideForClosing(
            type = type,
            exiting = predictiveClosing,
            entering = predictiveActive,
        ) ?: when(predictiveActive.navigationDirection) {
            NavigationDirection.Present -> defaults.presentReturn
            NavigationDirection.Push -> defaults.pushReturn
        }
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

    private fun <T: NavigationAnimation> getDefaultAnimationFor(
        defaults: NavigationAnimation.Defaults<T>,
        state: AnimationState,
        instruction: AnyOpenInstruction,
    ): T {
        return when (state) {
            AnimationState.Entering -> when (instruction.navigationDirection) {
                NavigationDirection.Present -> defaults.present
                NavigationDirection.Push -> defaults.push
            }
            AnimationState.Exiting -> when (instruction.navigationDirection) {
                NavigationDirection.Present -> defaults.present
                NavigationDirection.Push -> defaults.push
            }
            AnimationState.ReturnEnter -> when (instruction.navigationDirection) {
                NavigationDirection.Present -> defaults.presentReturn
                NavigationDirection.Push -> defaults.pushReturn
            }
            AnimationState.ReturnExit -> when (instruction.navigationDirection) {
                NavigationDirection.Present -> defaults.presentReturn
                NavigationDirection.Push -> defaults.pushReturn
            }
            AnimationState.None -> defaults.none
        }
    }

    enum class AnimationState {
        Entering,    // Instruction is becoming active/visible
        Exiting,     // Instruction is no longer active/visible
        ReturnEnter, // Instruction is returning to view (going back)
        ReturnExit,  // Instruction is exiting during a back navigation
        None,        // No animation needed
    }
}
