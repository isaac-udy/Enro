package dev.enro.core

import android.content.Context
import android.content.res.Resources
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Transition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import dev.enro.core.compose.animation.EnroAnimatedVisibility
import dev.enro.core.container.originalNavigationDirection
import dev.enro.extensions.getAttributeResourceId
import dev.enro.extensions.getNestedAttributeResourceId

@Deprecated("Please use NavigationAnimation")
public typealias AnimationPair = NavigationAnimation

public sealed class NavigationAnimation {
    public sealed class ForView : NavigationAnimation()

    public data class Resource(
        public val id: Int
    ) : ForView() {
        public fun isAnim(context: Context): Boolean = runCatching {
            context.resources.getResourceTypeName(id) == "anim"
        }.getOrDefault(false)

        public fun isAnimator(context: Context): Boolean = runCatching {
            context.resources.getResourceTypeName(id) == "animator"
        }.getOrDefault(false)
    }

    public data class Attr(
        public val attr: Int,
    ) : ForView()

    public data class Theme(
        public val id: (Resources.Theme) -> Int,
    ) : ForView()

    public sealed class Composable : NavigationAnimation() {
        internal abstract val forView: ForView

        @androidx.compose.runtime.Composable
        public abstract fun Animate(
            visible: Transition<Boolean>,
            content: @androidx.compose.runtime.Composable () -> Unit
        )

        public companion object {
            public operator fun invoke(
                enter: EnterTransition,
                exit: ExitTransition,
                forView: ForView = DefaultAnimations.ForView.noneEnter,
            ): Composable = EnterExit(enter, exit, forView)

            public operator fun invoke(
                forView: ForView,
            ): Composable = FromView(forView)
        }

        @Immutable
        internal data class EnterExit(
            val enter: EnterTransition = EnterTransition.None,
            val exit: ExitTransition = ExitTransition.None,
            override val forView: ForView = DefaultAnimations.ForView.noneEnter,
        ): Composable() {
            @OptIn(ExperimentalAnimationApi::class)
            @androidx.compose.runtime.Composable
            override fun Animate(visible: Transition<Boolean>, content: @androidx.compose.runtime.Composable () -> Unit) {
                visible.AnimatedVisibility(
                    visible = { it },
                    enter = enter,
                    exit = exit,
                    modifier = Modifier.fillMaxSize()
                ) {
                    content()
                }
            }
        }

        @Immutable
        internal data class FromView(
            override val forView: ForView
        ): Composable() {
            @androidx.compose.runtime.Composable
            override fun Animate(visible: Transition<Boolean>, content: @androidx.compose.runtime.Composable () -> Unit) {
                EnroAnimatedVisibility(
                    visibleState = visible,
                    animations = forView
                ) {
                    content()
                }
            }
        }

        @Immutable
        internal object NoAnimation : Composable() {
            override val forView: ForView = Resource(0)

            @androidx.compose.runtime.Composable
            override fun Animate(visible: Transition<Boolean>, content: @androidx.compose.runtime.Composable () -> Unit) {
                if (!visible.currentState && !visible.targetState) return
                content()
            }
        }
    }

    public fun asResource(theme: Resources.Theme): Resource = when (this) {
        is Resource -> this
        is Attr -> Resource(
            theme.getAttributeResourceId(attr),
        )
        is Theme -> Resource(
            id(theme),
        )
        is Composable -> forView.asResource(theme)
    }

    public fun asComposable(): Composable {
        return when (this) {
            is ForView -> Composable(forView = this)
            is Composable -> this
        }
    }
}

public data class NavigationAnimationTransition(
    public val entering: NavigationAnimation,
    public val exiting: NavigationAnimation,
)

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
        if(entering.originalNavigationDirection() == NavigationDirection.ReplaceRoot) {
            return NavigationAnimationTransition(
                entering = ForView.replaceRootEnter,
                exiting = ForView.replaceRootExit
            )
        }

        val enteringAnimation = when (entering.originalNavigationDirection()) {
            NavigationDirection.Push, NavigationDirection.Forward -> ForView.pushEnter
            else -> ForView.presentEnter
        }

        val exitingAnimation = when (exiting?.originalNavigationDirection()) {
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
        val enteringAnimation = when(entering?.originalNavigationDirection()) {
            null -> ForView.noneCloseExit
            NavigationDirection.ReplaceRoot -> when(exiting.originalNavigationDirection()) {
                NavigationDirection.Present -> ForView.presentCloseEnter
                else -> ForView.pushCloseEnter
            }
            NavigationDirection.Push, NavigationDirection.Forward -> ForView.pushCloseEnter
            else -> ForView.presentCloseEnter
        }

        val exitingAnimation = when (exiting.originalNavigationDirection()) {
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
            attr = android.R.attr.activityOpenEnterAnimation,
        )

        public val pushExit: NavigationAnimation.ForView = NavigationAnimation.Attr(
            attr = android.R.attr.activityOpenExitAnimation
        )

        public val pushCloseEnter: NavigationAnimation.ForView = NavigationAnimation.Attr(
            attr = android.R.attr.activityCloseEnterAnimation,
        )

        public val pushCloseExit: NavigationAnimation.ForView = NavigationAnimation.Attr(
            attr = android.R.attr.activityCloseExitAnimation
        )

        public val presentEnter: NavigationAnimation.ForView = NavigationAnimation.Theme(
            id = { theme ->
                if (Build.VERSION.SDK_INT >= 33) {
                    theme.getNestedAttributeResourceId(
                        android.R.attr.dialogTheme,
                        android.R.attr.windowAnimationStyle,
                        android.R.attr.windowEnterAnimation
                    ) ?: theme.getAttributeResourceId(android.R.attr.activityOpenEnterAnimation)
                } else {
                    theme.getAttributeResourceId(android.R.attr.activityOpenEnterAnimation)
                }
            }
        )

        public val presentExit: NavigationAnimation.ForView = NavigationAnimation.Theme(
            id = { theme ->
                    if (Build.VERSION.SDK_INT >= 33) {
                        theme.getNestedAttributeResourceId(
                            android.R.attr.dialogTheme,
                            android.R.attr.windowAnimationStyle,
                            android.R.attr.windowExitAnimation
                        ) ?: theme.getAttributeResourceId(android.R.attr.activityOpenExitAnimation)
                    } else {
                        theme.getAttributeResourceId(android.R.attr.activityOpenExitAnimation)
                    }
                }
        )

        public val presentCloseEnter: NavigationAnimation.ForView = NavigationAnimation.Theme(
            id = { theme ->
                if (Build.VERSION.SDK_INT >= 33) {
                    theme.getNestedAttributeResourceId(
                        android.R.attr.dialogTheme,
                        android.R.attr.windowAnimationStyle,
                        android.R.attr.windowEnterAnimation
                    ) ?: theme.getAttributeResourceId(android.R.attr.activityCloseEnterAnimation)
                } else {
                    theme.getAttributeResourceId(android.R.attr.activityCloseEnterAnimation)
                }
            }
        )

        public val presentCloseExit: NavigationAnimation.ForView = NavigationAnimation.Theme(
            id = { theme ->
                if (Build.VERSION.SDK_INT >= 33) {
                    theme.getNestedAttributeResourceId(
                        android.R.attr.dialogTheme,
                        android.R.attr.windowAnimationStyle,
                        android.R.attr.windowExitAnimation
                    ) ?: theme.getAttributeResourceId(android.R.attr.activityCloseExitAnimation)
                } else {
                    theme.getAttributeResourceId(android.R.attr.activityCloseExitAnimation)
                }
            }
        )

        public val replaceRootEnter: NavigationAnimation.ForView = NavigationAnimation.Attr(
            attr = android.R.attr.taskOpenEnterAnimation,
        )

        public val replaceRootExit: NavigationAnimation.ForView = NavigationAnimation.Attr(
            attr = android.R.attr.taskOpenExitAnimation
        )

        public val noneEnter: NavigationAnimation.ForView = NavigationAnimation.Resource(
            id = 0
        )

        public val noneExit: NavigationAnimation.ForView = NavigationAnimation.Resource(
            id = R.anim.enro_no_op_exit_animation
        )

        public val noneCloseEnter: NavigationAnimation.ForView = NavigationAnimation.Resource(
            id = 0
        )

        public val noneCloseExit: NavigationAnimation.ForView = NavigationAnimation.Resource(
            id = 0
        )
    }
}