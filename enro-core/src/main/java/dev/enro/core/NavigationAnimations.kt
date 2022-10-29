package dev.enro.core

import android.content.res.Resources
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import dev.enro.core.compose.animation.EnroAnimatedVisibility
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.usecase.GetNavigationExecutor
import dev.enro.core.controller.usecase.forClosing
import dev.enro.core.controller.usecase.forOpening
import dev.enro.core.hosts.AbstractActivityHostForAnyInstruction
import dev.enro.core.hosts.AbstractFragmentHostForComposable
import dev.enro.core.hosts.AbstractOpenComposableInFragmentKey
import dev.enro.core.hosts.AbstractOpenInstructionInActivityKey
import dev.enro.core.internal.get
import dev.enro.extensions.getAttributeResourceId
import dev.enro.extensions.getNestedAttributeResourceId

@Deprecated("Please use NavigationAnimation")
public typealias AnimationPair = NavigationAnimation

public sealed class NavigationAnimation {
    public sealed class ForView : NavigationAnimation()

    public class Resource(
        public val enter: Int,
        public val exit: Int
    ) : ForView()

    public class Attr(
        public val enter: Int,
        public val exit: Int
    ) : ForView()

    public class Theme(
        public val enter: (Resources.Theme) -> Int,
        public val exit: (Resources.Theme) -> Int
    ) : ForView()

    public class Composable private constructor(
        public val forView: ForView,
        public val content: @androidx.compose.runtime.Composable (
            visible: MutableTransitionState<Boolean>,
            content: @androidx.compose.runtime.Composable () -> Unit
        ) -> Unit
    ) : NavigationAnimation() {
        public constructor(
            enter: EnterTransition,
            exit: ExitTransition,
            forView: ForView
        ) : this(
            forView = forView,
            content = { visible, content ->
                AnimatedVisibility(
                    visibleState = visible,
                    enter = enter,
                    exit = exit,
                    modifier = Modifier.fillMaxSize(),
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
            theme.getAttributeResourceId(enter),
            theme.getAttributeResourceId(exit)
        )
        is Theme -> Resource(
            enter(theme),
            exit(theme)
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


public object DefaultAnimations {
    public val push: NavigationAnimation = DefaultAnimations.ForView.push
    public val present: NavigationAnimation = DefaultAnimations.ForView.present
    public val replaceRoot: NavigationAnimation = DefaultAnimations.ForView.replaceRoot
    public val close: NavigationAnimation = DefaultAnimations.ForView.close
    public val none: NavigationAnimation = DefaultAnimations.ForView.none

    @Deprecated("Use push or present")
    public val forward: NavigationAnimation = DefaultAnimations.ForView.forward

    @Deprecated("Use push or present")
    public val replace: NavigationAnimation = DefaultAnimations.ForView.replace

    public object ForView {
        public val push: NavigationAnimation.ForView = NavigationAnimation.Attr(
            enter = android.R.attr.activityOpenEnterAnimation,
            exit = android.R.attr.activityOpenExitAnimation
        )

        public val present: NavigationAnimation.ForView = NavigationAnimation.Theme(
            enter = { theme ->
                if (Build.VERSION.SDK_INT >= 33) {
                    theme.getNestedAttributeResourceId(
                        android.R.attr.dialogTheme,
                        android.R.attr.windowAnimationStyle,
                        android.R.attr.windowEnterAnimation
                    ) ?: theme.getAttributeResourceId(android.R.attr.activityOpenEnterAnimation)
                } else {
                    theme.getAttributeResourceId(android.R.attr.activityOpenEnterAnimation)
                }
            },
            exit = { theme ->
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

        @Deprecated("Use push or present")
        public val forward: NavigationAnimation.ForView = NavigationAnimation.Attr(
            enter = android.R.attr.activityOpenEnterAnimation,
            exit = android.R.attr.activityOpenExitAnimation
        )

        @Deprecated("Use push or present")
        public val replace: NavigationAnimation.ForView = NavigationAnimation.Attr(
            enter = android.R.attr.activityOpenEnterAnimation,
            exit = android.R.attr.activityOpenExitAnimation
        )

        public val replaceRoot: NavigationAnimation.ForView = NavigationAnimation.Attr(
            enter = android.R.attr.taskOpenEnterAnimation,
            exit = android.R.attr.taskOpenExitAnimation
        )

        public val close: NavigationAnimation.ForView = NavigationAnimation.Attr(
            enter = android.R.attr.activityCloseEnterAnimation,
            exit = android.R.attr.activityCloseExitAnimation
        )

        public val none: NavigationAnimation.ForView = NavigationAnimation.Resource(
            enter = 0,
            exit = R.anim.enro_no_op_exit_animation
        )
    }
}

public fun animationsFor(
    context: NavigationContext<*>,
    navigationInstruction: NavigationInstruction
): NavigationAnimation {
    val animationScale = runCatching {
        Settings.Global.getFloat(
            context.activity.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE
        )
    }.getOrDefault(1.0f)

    if (animationScale < 0.01f) {
        return NavigationAnimation.Resource(0, 0)
    }

    if (navigationInstruction is NavigationInstruction.Open<*> && navigationInstruction.children.isNotEmpty()) {
        return  NavigationAnimation.Resource(0, 0)
    }

    if (navigationInstruction is NavigationInstruction.Open<*> && context.contextReference is AbstractActivityHostForAnyInstruction) {
        val openActivityKey = context.getNavigationHandleViewModel().key as AbstractOpenInstructionInActivityKey
        if (navigationInstruction.instructionId == openActivityKey.instruction.instructionId) {
            return NavigationAnimation.Resource(0, 0)
        }
    }

    return when (navigationInstruction) {
        is NavigationInstruction.Open<*> -> animationsForOpen(context.controller, navigationInstruction)
        is NavigationInstruction.Close -> animationsForClose(context)
        is NavigationInstruction.RequestClose -> animationsForClose(context)
    }
}

private fun animationsForOpen(
    controller: NavigationController,
    navigationInstruction: AnyOpenInstruction
): NavigationAnimation {
    val instructionForAnimation =  when (
        val navigationKey = navigationInstruction.navigationKey
    ) {
        is AbstractOpenComposableInFragmentKey -> navigationKey.instruction
        else -> navigationInstruction
    }

    val executor = controller.dependencyScope.get<GetNavigationExecutor>().forOpening(
        instructionForAnimation
    )
    return executor.animation(navigationInstruction)
}

private fun animationsForClose(
    context: NavigationContext<*>
): NavigationAnimation {
    val contextForAnimation = when (context.contextReference) {
        is AbstractFragmentHostForComposable -> {
            context.containerManager
                .activeContainer
                ?.activeContext
                ?: context
        }
        else -> context
    }

    val executor = context.controller.dependencyScope.get<GetNavigationExecutor>().forClosing(
        contextForAnimation
    )
    return executor.closeAnimation(context)
}