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
    private var containerAnimation by mutableStateOf<NavigationAnimation.Composable?>(NavigationAnimation.None.asComposable())

    private var animationOverride by mutableStateOf<NavigationAnimation.Composable?>(null)

    internal lateinit var enterExitTransition: Transition<EnterExitState>

    internal fun setAnimation(animation: NavigationAnimation.Composable) {
        containerAnimation = animation
    }

    internal fun setAnimationOverride(animation: NavigationAnimation.Composable) {
        animationOverride = animation
    }

    internal fun setAnimationEvent(event: AnimationEvent) {
        currentAnimationEvent = event
    }

    @Composable
    @NonSkippableComposable
    fun Animate(content: @Composable () -> Unit) {
        val instruction = owner.instruction
        val visibilityState = remember(instruction.instructionId, animationOverride.hashCode()) {
            SeekableTransitionState(false)
        }
        val animation = remember(
            containerAnimation,
            animationOverride
        ) {
           animationOverride
                ?: containerAnimation
                ?: return@remember null
        }
        if (animation == null) return

        animation.Animate(
            state = visibilityState,
            isSeeking = currentAnimationEvent is AnimationEvent.Seek
        ) {
            enterExitTransition = it
            content()
        }

        LaunchedEffect(currentAnimationEvent, visibilityState) {
            val event = currentAnimationEvent
            runCatching {
                when (event) {
                    is AnimationEvent.AnimateTo -> visibilityState.animateTo(event.visible)
                    is AnimationEvent.SnapTo -> visibilityState.snapTo(event.visible)
                    is AnimationEvent.Seek -> visibilityState.seekTo(event.progress, event.visible)
                }
            }
        }
    }
}