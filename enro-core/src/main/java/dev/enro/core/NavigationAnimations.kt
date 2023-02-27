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
import androidx.compose.ui.Modifier
import dev.enro.core.compose.animation.EnroAnimatedVisibility
import dev.enro.extensions.getAttributeResourceId
import dev.enro.extensions.getNestedAttributeResourceId

@Deprecated("Please use NavigationAnimation")
public typealias AnimationPair = NavigationAnimation

public sealed class NavigationAnimation {
    public sealed class ForView : NavigationAnimation()

    public class Resource(
        public val id: Int
    ) : ForView() {
        public fun isAnim(context: Context): Boolean = runCatching {
            context.resources.getResourceTypeName(id) == "anim"
        }.getOrDefault(false)

        public fun isAnimator(context: Context): Boolean = runCatching {
            context.resources.getResourceTypeName(id) == "animator"
        }.getOrDefault(false)
    }

    public class Attr(
        public val attr: Int,
    ) : ForView()

    public class Theme(
        public val id: (Resources.Theme) -> Int,
    ) : ForView()

    public class Composable private constructor(
        public val forView: ForView,
        public val content: @androidx.compose.runtime.Composable (
            visible: Transition<Boolean>,
            content: @androidx.compose.runtime.Composable () -> Unit
        ) -> Unit
    ) : NavigationAnimation() {
        @OptIn(ExperimentalAnimationApi::class)
        public constructor(
            enter: EnterTransition = EnterTransition.None,
            exit: ExitTransition = ExitTransition.None,
            forView: ForView
        ) : this(
            forView = forView,
            content = { visible, content ->
                visible.AnimatedVisibility(
                    visible = { it },
                    enter = enter,
                    exit = exit,
                    modifier = Modifier.fillMaxSize()
                ) {
                    content()
                }
            }
        )

        public constructor(
            forView: ForView
        ) : this(
            forView = forView,
            content = { visible, content ->
                EnroAnimatedVisibility(
                    visibleState = visible,
                    animations = forView
                ) {
                    content()
                }
            }
        )
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
    public val push: NavigationAnimationTransition = NavigationAnimationTransition(
        entering = ForView.pushEnter,
        exiting = ForView.pushExit,
    )

    public val pushClose: NavigationAnimationTransition = NavigationAnimationTransition(
        entering = ForView.pushCloseEnter,
        exiting = ForView.pushCloseExit,
    )

    public val present: NavigationAnimationTransition = NavigationAnimationTransition(
        entering = ForView.presentEnter,
        exiting = ForView.presentExit,
    )

    public val presentClose: NavigationAnimationTransition = NavigationAnimationTransition(
        entering = ForView.presentCloseEnter,
        exiting = ForView.presentCloseExit,
    )

    public val replaceRoot: NavigationAnimationTransition = NavigationAnimationTransition(
        entering = ForView.replaceRootEnter,
        exiting = ForView.replaceRootExit,
    )

    public val none: NavigationAnimationTransition = NavigationAnimationTransition(
        entering = ForView.noneEnter,
        exiting = ForView.noneExit,
    )

    public val noneClose: NavigationAnimationTransition = NavigationAnimationTransition(
        entering = ForView.noneCloseEnter,
        exiting = ForView.noneCloseExit,
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