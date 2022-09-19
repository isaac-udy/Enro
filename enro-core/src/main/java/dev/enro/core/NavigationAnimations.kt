package dev.enro.core

import android.content.res.Resources
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import dev.enro.core.compose.animation.EnroAnimatedVisibility
import dev.enro.core.hosts.AbstractFragmentHostForComposable
import dev.enro.core.hosts.AbstractOpenComposableInFragmentKey
import dev.enro.core.controller.navigationController
import dev.enro.core.hosts.AbstractActivityHostForAnyInstruction
import dev.enro.core.hosts.AbstractOpenInstructionInActivityKey
import dev.enro.extensions.getAttributeResourceId
import dev.enro.extensions.getNestedAttributeResourceId

@Deprecated("Please use NavigationAnimation")
typealias AnimationPair = NavigationAnimation

sealed class NavigationAnimation {
    sealed class ForView: NavigationAnimation()

    class Resource(
        val enter: Int,
        val exit: Int
    ) : ForView()

    class Attr(
        val enter: Int,
        val exit: Int
    ) : ForView()

    class Theme(
        val enter: (Resources.Theme) -> Int,
        val exit: (Resources.Theme) -> Int
    ) : ForView()

    class Composable(
        val forView: ForView,
        val content: @androidx.compose.runtime.Composable (
            visible: Boolean,
            content: @androidx.compose.runtime.Composable () -> Unit
        ) -> Unit
    ): NavigationAnimation() {
        constructor(
            enter: EnterTransition,
            exit: ExitTransition,
            forView: ForView
        ) : this(
            forView = forView,
            content = { visible, content ->
                AnimatedVisibility(
                    visible = visible,
                    enter = enter,
                    exit = exit,
                    modifier = Modifier.fillMaxSize()
                ) {
                    content()
                }
            }
        )
    }

    fun asResource(theme: Resources.Theme): Resource = when (this) {
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

    fun asComposable() : Composable {
        return when (this) {
            is Resource,
            is Theme,
            is Attr -> Composable(
                forView = DefaultAnimations.none,
                content = { visible, content ->
                    EnroAnimatedVisibility(
                        visible = visible,
                        animations = this
                    ) {
                        content()
                    }
                }
            )
            is Composable -> this
        }
    }
}

object DefaultAnimations {
    val push = NavigationAnimation.Attr(
        enter = android.R.attr.activityOpenEnterAnimation,
        exit = android.R.attr.activityOpenExitAnimation
    )

    val present = NavigationAnimation.Theme(
        enter = { theme ->
            theme.getNestedAttributeResourceId(
                android.R.attr.dialogTheme,
                android.R.attr.windowAnimationStyle,
                android.R.attr.windowEnterAnimation
            ) ?: 0
        },
        exit = { theme ->
            theme.getNestedAttributeResourceId(
                android.R.attr.dialogTheme,
                android.R.attr.windowAnimationStyle,
                android.R.attr.windowExitAnimation
            ) ?: 0
        }
    )

    @Deprecated("Use push or present")
    val forward = NavigationAnimation.Attr(
        enter = android.R.attr.activityOpenEnterAnimation,
        exit = android.R.attr.activityOpenExitAnimation
    )

    @Deprecated("Use push or present")
    val replace = NavigationAnimation.Attr(
        enter = android.R.attr.activityOpenEnterAnimation,
        exit = android.R.attr.activityOpenExitAnimation
    )

    val replaceRoot = NavigationAnimation.Attr(
        enter = android.R.attr.taskOpenEnterAnimation,
        exit = android.R.attr.taskOpenExitAnimation
    )

    val close = NavigationAnimation.Attr(
        enter = android.R.attr.activityCloseEnterAnimation,
        exit = android.R.attr.activityCloseExitAnimation
    )

    val none = NavigationAnimation.Resource(
        enter = 0,
        exit = R.anim.enro_no_op_animation
    )
}

fun animationsFor(
    context: NavigationContext<*>,
    navigationInstruction: NavigationInstruction
): NavigationAnimation {
    val animationScale = runCatching {
        Settings.Global.getFloat(context.activity.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE)
    }.getOrDefault(1.0f)

    if(animationScale < 0.01f) {
        return NavigationAnimation.Resource(0, 0)
    }
    if (navigationInstruction is NavigationInstruction.Open<*> && navigationInstruction.children.isNotEmpty()) {
        return NavigationAnimation.Resource(0, 0)
    }

    if (navigationInstruction is NavigationInstruction.Open<*> && context.contextReference is AbstractActivityHostForAnyInstruction) {
        val openActivityKey = context.getNavigationHandleViewModel().key as AbstractOpenInstructionInActivityKey
        if (navigationInstruction.instructionId == openActivityKey.instruction.instructionId) {
            return NavigationAnimation.Resource(0, 0)
        }
    }

    if (navigationInstruction is NavigationInstruction.Open<*> && context.contextReference is AbstractFragmentHostForComposable) {
        val openFragmentKey = context.getNavigationHandleViewModel().key as AbstractOpenComposableInFragmentKey
        if (navigationInstruction.instructionId == openFragmentKey.instruction.instructionId) {
            return NavigationAnimation.Resource(0, 0)
        }
    }

    return when (navigationInstruction) {
        is NavigationInstruction.Open<*> -> animationsForOpen(context, navigationInstruction)
        is NavigationInstruction.Close -> animationsForClose(context)
        is NavigationInstruction.RequestClose -> animationsForClose(context)
    }
}

private fun animationsForOpen(
    context: NavigationContext<*>,
    navigationInstruction: AnyOpenInstruction
): NavigationAnimation {
    val instructionForAnimation =  when (
        val navigationKey = navigationInstruction.navigationKey
    ) {
        is AbstractOpenComposableInFragmentKey -> navigationKey.instruction
        else -> navigationInstruction
    }

    val executor = context.activity.application.navigationController.executorForOpen(
        context,
        instructionForAnimation
    )
    return executor.executor.animation(navigationInstruction)
}

private fun animationsForClose(
    context: NavigationContext<*>
): NavigationAnimation {
    val contextForAnimation = when (context.contextReference) {
        is AbstractFragmentHostForComposable -> {
            context.containerManager.containers
                .firstOrNull()
                ?.activeContext
                ?: context
        }
        else -> context
    }

    val executor = context.activity.application.navigationController.executorForClose(contextForAnimation)
    return executor.closeAnimation(context)
}