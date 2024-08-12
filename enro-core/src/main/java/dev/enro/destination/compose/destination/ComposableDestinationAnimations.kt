package dev.enro.destination.compose.destination

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.enro.animation.NavigationAnimation
import dev.enro.core.compose.destination.ComposableDestinationOwner
import dev.enro.core.compose.dialog.BottomSheetDestination
import dev.enro.core.compose.dialog.DialogDestination
import dev.enro.core.container.getAnimationsForEntering
import dev.enro.core.container.getAnimationsForExiting

internal sealed class AnimationEvent {
    data class AnimateTo(val visible: Boolean) : AnimationEvent()
    data class SnapTo(val visible: Boolean) : AnimationEvent()
    data class Seek(val progress: Float, val visible: Boolean) : AnimationEvent()
}

internal class ComposableDestinationAnimations(
    private val owner: ComposableDestinationOwner,
) {
    private var currentAnimationEvent by mutableStateOf<AnimationEvent>(AnimationEvent.SnapTo(false))
    private val visibilityState = SeekableTransitionState(false)

    internal var animationOverride by mutableStateOf<NavigationAnimation.Composable?>(null)

    internal lateinit var enterExitTransition: Transition<EnterExitState>

    val isAnimating by derivedStateOf {
        when (currentAnimationEvent) {
            is AnimationEvent.AnimateTo -> visibilityState.targetState != visibilityState.currentState
            is AnimationEvent.SnapTo -> false
            is AnimationEvent.Seek -> true
        }
    }

    internal fun setAnimationEvent(event: AnimationEvent) {
        currentAnimationEvent = event
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun Animate(content: @Composable () -> Unit) {
        val targetState = visibilityState.targetState
        val instruction = owner.instruction
        val parentContainer = owner.parentContainer

        val animation = remember(
            instruction,
            targetState,
            parentContainer,
            animationOverride
        ) {
            animationOverride ?: when (owner.destination) {
                is DialogDestination -> NavigationAnimation.Composable(EnterTransition.None, ExitTransition.None)
                is BottomSheetDestination -> NavigationAnimation.Composable(
                    enter = EnterTransition.None,
                    exit = fadeOut(tween(75, 150)),
                )

                else -> when {
                    visibilityState.targetState >= visibilityState.currentState -> parentContainer.getAnimationsForEntering(
                        instruction
                    ).asComposable()

                    else -> parentContainer.getAnimationsForExiting(instruction).asComposable()
                }
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
            currentAnimationEvent = AnimationEvent.SnapTo(visibilityState.targetState)
        }

        animation.Animate(
            visible = rememberTransition(visibilityState, "ComposableDestination Visibility"),
        ) {
            enterExitTransition = it
            content()
        }
    }
}