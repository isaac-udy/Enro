package dev.enro.animation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import dev.enro.core.AnyOpenInstruction

public object DefaultAnimations {
    // TODO add explaination of difference between none and no-op,
    // and properly implement no-op as doing a "hold" on closing
    public val none: NavigationAnimationTransition = NavigationAnimationTransition(
        entering = NavigationAnimation.Composable(
            enter = EnterTransition.None,
            exit = ExitTransition.None,
        ),
        exiting = NavigationAnimation.Composable(
            enter = EnterTransition.None,
            exit = ExitTransition.None,
        ),
    )

    public val noOp: NavigationAnimationTransition = NavigationAnimationTransition(
        entering = NavigationAnimation.Composable(
            enter = EnterTransition.None,
            exit = ExitTransition.None,
        ),
        exiting = NavigationAnimation.Composable(
            enter = EnterTransition.None,
            exit = ExitTransition.None,
        )
    )

    public fun opening(exiting: AnyOpenInstruction?, entering: AnyOpenInstruction): NavigationAnimationTransition {
        val enteringAnimation = NavigationAnimation.Composable(
            enter = fadeIn(tween(125)),
            exit = fadeOut(tween(16, delayMillis = 125)),
        )

        val exitingAnimation = NavigationAnimation.Composable(
            enter = fadeIn(tween(125)),
            exit = fadeOut(tween(16, delayMillis = 125)),
        )

        return NavigationAnimationTransition(
            entering = enteringAnimation,
            exiting = exitingAnimation
        )
    }

    public fun closing(exiting: AnyOpenInstruction, entering: AnyOpenInstruction?): NavigationAnimationTransition {
        val enteringAnimation = NavigationAnimation.Composable(
            enter = fadeIn(tween(16)),
            exit = fadeOut(tween(125, delayMillis = 16)),
        )

        val exitingAnimation = NavigationAnimation.Composable(
            enter = fadeIn(tween(16)),
            exit = fadeOut(tween(125, delayMillis = 16)),
        )

        return NavigationAnimationTransition(
            entering = enteringAnimation,
            exiting = exitingAnimation
        )
    }
}