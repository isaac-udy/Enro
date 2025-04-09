package dev.enro.animation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationDirection
import dev.enro.core.container.originalNavigationDirection

public object DefaultAnimations {
    public val none: NavigationAnimationTransition = NavigationAnimationTransition(
        entering = ForView.noneEnter,
        exiting = ForView.noneExit,
    )

    public val noOp: NavigationAnimationTransition = NavigationAnimationTransition(
        entering = NavigationAnimation.Composable(
            forView = NavigationAnimation.None,
            enter = EnterTransition.None,
            exit = ExitTransition.None,
        ),
        exiting = NavigationAnimation.Composable(
            forView = NavigationAnimation.None,
            enter = EnterTransition.None,
            exit = ExitTransition.None,
        )
    )

    public fun opening(exiting: AnyOpenInstruction?, entering: AnyOpenInstruction): NavigationAnimationTransition {
        val enteringAnimation = when (entering.originalNavigationDirection()) {
            NavigationDirection.Push -> ForView.pushEnter
            else -> ForView.presentEnter
        }

        val exitingAnimation = when (exiting?.navigationDirection) {
            null -> ForView.noneExit
            NavigationDirection.Push -> ForView.pushExit
            else -> ForView.presentExit
        }

        return NavigationAnimationTransition(
            entering = enteringAnimation,
            exiting = exitingAnimation
        )
    }

    public fun closing(exiting: AnyOpenInstruction, entering: AnyOpenInstruction?): NavigationAnimationTransition {
        val enteringAnimation = when (entering?.navigationDirection) {
            null -> ForView.noneCloseExit
            NavigationDirection.Present -> ForView.presentCloseEnter
            else -> ForView.pushCloseEnter
        }

        val exitingAnimation = when (exiting.navigationDirection) {
            else -> ForView.presentCloseExit
        }

        return NavigationAnimationTransition(
            entering = enteringAnimation,
            exiting = exitingAnimation
        )
    }

    public object ForView {
        public val pushEnter: NavigationAnimation.ForView = NavigationAnimation.None

        public val pushExit: NavigationAnimation.ForView = NavigationAnimation.None

        public val pushCloseEnter: NavigationAnimation.ForView = NavigationAnimation.None

        public val pushCloseExit: NavigationAnimation.ForView = NavigationAnimation.None

        public val presentEnter: NavigationAnimation.ForView = NavigationAnimation.None

        public val presentExit: NavigationAnimation.ForView = NavigationAnimation.None

        public val presentCloseEnter: NavigationAnimation.ForView = NavigationAnimation.None

        public val presentCloseExit: NavigationAnimation.ForView = NavigationAnimation.None

        public val replaceRootEnter: NavigationAnimation.ForView = NavigationAnimation.None

        public val replaceRootExit: NavigationAnimation.ForView = NavigationAnimation.None

        public val noneEnter: NavigationAnimation.ForView = NavigationAnimation.None

        public val noneExit: NavigationAnimation.ForView = NavigationAnimation.None

        public val noneCloseEnter: NavigationAnimation.ForView = NavigationAnimation.None

        public val noneCloseExit: NavigationAnimation.ForView = NavigationAnimation.None
    }
}