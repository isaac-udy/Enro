package dev.enro.destination.compose.destination

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.Transition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonSkippableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.enro.animation.NavigationAnimation
import dev.enro.core.compose.destination.ComposableDestinationOwner
import dev.enro.core.container.getAnimationsForEntering
import dev.enro.core.container.getAnimationsForExiting

internal sealed class AnimationEvent {
    abstract val visible: Boolean

    data class AnimateTo(override val visible: Boolean) : AnimationEvent()
    data class SnapTo(override val visible: Boolean) : AnimationEvent()
    data class Seek(val progress: Float, override val visible: Boolean) : AnimationEvent()
}

internal class ComposableDestinationAnimations(
    private val owner: ComposableDestinationOwner,
) {
    private var currentAnimationEvent by mutableStateOf<AnimationEvent>(AnimationEvent.SnapTo(false))

    internal var animationOverride by mutableStateOf<NavigationAnimation.Composable?>(null)

    internal lateinit var enterExitTransition: Transition<EnterExitState>

    internal fun setAnimationEvent(event: AnimationEvent) {
        currentAnimationEvent = event
    }

    @Composable
    @NonSkippableComposable
    fun Animate(content: @Composable () -> Unit) {
        val instruction = owner.instruction
        val parentContainer = owner.parentContainer

        val visibilityState = remember(instruction.instructionId) { SeekableTransitionState(false) }
        val targetState = visibilityState.targetState

        val animation = remember(
            instruction,
            targetState,
            parentContainer,
            animationOverride
        ) {
            animationOverride ?: when {
                visibilityState.targetState >= visibilityState.currentState -> parentContainer.getAnimationsForEntering(
                    instruction
                ).asComposable()

                else -> parentContainer.getAnimationsForExiting(instruction).asComposable()
            }
        }

        LaunchedEffect(currentAnimationEvent) {
            val event = currentAnimationEvent
            runCatching {
                when (event) {
                    is AnimationEvent.AnimateTo -> visibilityState.animateTo(event.visible)
                    is AnimationEvent.SnapTo -> visibilityState.snapTo(event.visible)
                    is AnimationEvent.Seek -> visibilityState.seekTo(event.progress, event.visible)
                }
            }
            // If we're not seeking, we should snap to the target state as the final task, to make
            // sure we're in the correct state.
            if (currentAnimationEvent == event && event !is AnimationEvent.Seek) {
                currentAnimationEvent = AnimationEvent.SnapTo(event.visible)
            }
        }
        animation.Animate(
            state = visibilityState,
            isSeeking = currentAnimationEvent is AnimationEvent.Seek
        ) {
            enterExitTransition = it
            content()
        }
    }
}