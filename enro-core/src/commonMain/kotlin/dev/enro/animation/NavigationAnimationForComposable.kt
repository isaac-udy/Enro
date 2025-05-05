package dev.enro.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import dev.enro.destination.compose.LocalAnimatedVisibilityScope

public data class NavigationAnimationForComposable(
    public val enter: EnterTransition = EnterTransition.None,
    public val exit: ExitTransition = ExitTransition.None,
) : NavigationAnimation {
    public companion object {
        public val Defaults: NavigationAnimation.Defaults<NavigationAnimationForComposable> = NavigationAnimation.Defaults(
            none = NavigationAnimationForComposable(
                enter = EnterTransition.None,
                exit = ExitTransition.None,
            ),
            push = NavigationAnimationForComposable(
                enter = fadeIn(tween(125)),
                exit = fadeOut(tween(16, delayMillis = 125)),
            ),
            pushReturn = NavigationAnimationForComposable(
                enter = fadeIn(tween(16)),
                exit = fadeOut(tween(125, delayMillis = 16)),
            ),
            present = NavigationAnimationForComposable(
                enter = fadeIn(tween(125)),
                exit = fadeOut(tween(16, delayMillis = 125)),
            ),
            presentReturn = NavigationAnimationForComposable(
                enter = fadeIn(tween(16)),
                exit = fadeOut(tween(125, delayMillis = 16)),
            ),
        )
    }
}

@Composable
internal fun AnimateNavigationAnimations(
    animation: NavigationAnimationForComposable,
    state: SeekableTransitionState<Boolean>,
    isSeeking: Boolean,
    content: @Composable AnimatedVisibilityScope.(Transition<EnterExitState>) -> Unit,
) {
    val visible = rememberTransition(state, "ComposableDestination Visibility")
    visible.AnimatedVisibility(
        visible = { it },
        enter = animation.enter,
        exit = animation.exit,
    ) {
        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
            content(transition)
        }
    }
}