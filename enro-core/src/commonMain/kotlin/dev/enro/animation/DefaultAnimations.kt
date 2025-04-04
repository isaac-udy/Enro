package dev.enro.animation

import android.R
import android.os.Build
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationDirection
import dev.enro.core.container.originalNavigationDirection
import dev.enro.extensions.getAttributeResourceId
import dev.enro.extensions.getNestedAttributeResourceId

public object DefaultAnimations {
    public val none: NavigationAnimationTransition = NavigationAnimationTransition(
        entering = ForView.noneEnter,
        exiting = ForView.noneExit,
    )

    public val noOp: NavigationAnimationTransition = NavigationAnimationTransition(
        entering = NavigationAnimation.Composable(
            forView = NavigationAnimation.Resource(0),
            enter = EnterTransition.None,
            exit = ExitTransition.None,
        ),
        exiting = NavigationAnimation.Composable(
            forView = NavigationAnimation.Resource(0),
            enter = EnterTransition.None,
            exit = ExitTransition.None,
        )
    )

    public fun opening(exiting: AnyOpenInstruction?, entering: AnyOpenInstruction): NavigationAnimationTransition {
        if (entering.originalNavigationDirection() == NavigationDirection.ReplaceRoot) {
            return NavigationAnimationTransition(
                entering = ForView.replaceRootEnter,
                exiting = ForView.replaceRootExit
            )
        }

        val enteringAnimation = when (entering.originalNavigationDirection()) {
            NavigationDirection.Push, NavigationDirection.Forward -> ForView.pushEnter
            else -> ForView.presentEnter
        }

        val exitingAnimation = when (exiting?.navigationDirection) {
            null -> ForView.noneExit
            NavigationDirection.Push, NavigationDirection.Forward -> ForView.pushExit
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
            NavigationDirection.ReplaceRoot -> when (exiting.originalNavigationDirection()) {
                NavigationDirection.Present -> ForView.presentCloseEnter
                else -> ForView.pushCloseEnter
            }

            NavigationDirection.Push, NavigationDirection.Forward -> ForView.pushCloseEnter
            else -> ForView.presentCloseEnter
        }

        val exitingAnimation = when (exiting.navigationDirection) {
            NavigationDirection.Push, NavigationDirection.Forward -> ForView.pushCloseExit
            else -> ForView.presentCloseExit
        }

        return NavigationAnimationTransition(
            entering = enteringAnimation,
            exiting = exitingAnimation
        )
    }

    public object ForView {
        public val pushEnter: NavigationAnimation.ForView = NavigationAnimation.Attr(
            attr = R.attr.activityOpenEnterAnimation,
        )

        public val pushExit: NavigationAnimation.ForView = NavigationAnimation.Attr(
            attr = R.attr.activityOpenExitAnimation
        )

        public val pushCloseEnter: NavigationAnimation.ForView = NavigationAnimation.Attr(
            attr = R.attr.activityCloseEnterAnimation,
        )

        public val pushCloseExit: NavigationAnimation.ForView = NavigationAnimation.Attr(
            attr = R.attr.activityCloseExitAnimation
        )

        public val presentEnter: NavigationAnimation.ForView = NavigationAnimation.Theme(
            id = { theme ->
                if (Build.VERSION.SDK_INT >= 33) {
                    theme.getNestedAttributeResourceId(
                        R.attr.dialogTheme,
                        R.attr.windowAnimationStyle,
                        R.attr.windowEnterAnimation
                    ) ?: theme.getAttributeResourceId(R.attr.activityOpenEnterAnimation)
                } else {
                    theme.getAttributeResourceId(R.attr.activityOpenEnterAnimation)
                }
            }
        )

        public val presentExit: NavigationAnimation.ForView = NavigationAnimation.Theme(
            id = { theme ->
                if (Build.VERSION.SDK_INT >= 33) {
                    theme.getNestedAttributeResourceId(
                        R.attr.dialogTheme,
                        R.attr.windowAnimationStyle,
                        R.attr.windowExitAnimation
                    ) ?: theme.getAttributeResourceId(R.attr.activityOpenExitAnimation)
                } else {
                    theme.getAttributeResourceId(R.attr.activityOpenExitAnimation)
                }
            }
        )

        public val presentCloseEnter: NavigationAnimation.ForView = NavigationAnimation.Theme(
            id = { theme ->
                if (Build.VERSION.SDK_INT >= 33) {
                    theme.getNestedAttributeResourceId(
                        R.attr.dialogTheme,
                        R.attr.windowAnimationStyle,
                        R.attr.windowEnterAnimation
                    ) ?: theme.getAttributeResourceId(R.attr.activityOpenEnterAnimation)
                } else {
                    theme.getAttributeResourceId(R.attr.activityOpenEnterAnimation)
                }
            }
        )

        public val presentCloseExit: NavigationAnimation.ForView = NavigationAnimation.Theme(
            id = { theme ->
                if (Build.VERSION.SDK_INT >= 33) {
                    theme.getNestedAttributeResourceId(
                        R.attr.dialogTheme,
                        R.attr.windowAnimationStyle,
                        R.attr.windowExitAnimation
                    ) ?: theme.getAttributeResourceId(R.attr.activityOpenExitAnimation)
                } else {
                    theme.getAttributeResourceId(R.attr.activityOpenExitAnimation)
                }
            }
        )

        public val replaceRootEnter: NavigationAnimation.ForView = NavigationAnimation.Attr(
            attr = R.attr.taskOpenEnterAnimation,
        )

        public val replaceRootExit: NavigationAnimation.ForView = NavigationAnimation.Attr(
            attr = R.attr.taskOpenExitAnimation
        )

        public val noneEnter: NavigationAnimation.ForView = NavigationAnimation.Resource(
            id = 0
        )

        public val noneExit: NavigationAnimation.ForView = NavigationAnimation.Resource(
            id = dev.enro.core.R.anim.enro_no_op_exit_animation
        )

        public val noneCloseEnter: NavigationAnimation.ForView = NavigationAnimation.Resource(
            id = 0
        )

        public val noneCloseExit: NavigationAnimation.ForView = NavigationAnimation.Resource(
            id = 0
        )
    }
}