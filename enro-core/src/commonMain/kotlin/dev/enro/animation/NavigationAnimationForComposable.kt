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
import androidx.compose.runtime.key
import dev.enro.destination.compose.LocalAnimatedVisibilityScope

public data class NavigationAnimationForComposable(
    public val enter: EnterTransition = EnterTransition.None,
    public val exit: ExitTransition = ExitTransition.None,
) : NavigationAnimation {
    public object Defaults: NavigationAnimation.Defaults<NavigationAnimationForComposable> {
        // TODO add explaination of difference between none and no-op,
        // and properly implement no-op as doing a "hold" on closing
        public override val none: NavigationAnimationForComposable = NavigationAnimationForComposable(
            enter = EnterTransition.None,
            exit = ExitTransition.None,
        )

        public override val push: NavigationAnimationForComposable = NavigationAnimationForComposable(
            enter = fadeIn(tween(125)),
            exit = fadeOut(tween(16, delayMillis = 125)),
        )

        public override val pushReturn: NavigationAnimationForComposable = NavigationAnimationForComposable(
            enter = fadeIn(tween(16)),
            exit = fadeOut(tween(125, delayMillis = 16)),
        )

        public override val present: NavigationAnimationForComposable = NavigationAnimationForComposable(
            enter = fadeIn(tween(125)),
            exit = fadeOut(tween(16, delayMillis = 125)),
        )

        public override val presentReturn: NavigationAnimationForComposable = NavigationAnimationForComposable(
            enter = fadeIn(tween(16)),
            exit = fadeOut(tween(125, delayMillis = 16)),
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
    key(animation) {
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
}