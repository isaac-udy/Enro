package dev.enro.core

import android.content.res.Resources
import android.provider.Settings
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import dev.enro.core.compose.AbstractComposeFragmentHost
import dev.enro.core.compose.AbstractComposeFragmentHostKey
import dev.enro.core.controller.navigationController
import dev.enro.core.fragment.internal.AbstractSingleFragmentActivity
import dev.enro.core.fragment.internal.AbstractSingleFragmentKey
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
        val fallback: ForView,
        val content: @androidx.compose.runtime.Composable (visible: Boolean) -> Unit
    ): NavigationAnimation() {
        constructor(
            enter: EnterTransition,
            exit: ExitTransition,
            fallback: ForView
        ) : this(
            fallback = fallback,
            content = {}
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
        is Composable -> fallback.asResource(theme)
    }

//    fun asComposable() : Composable = when (this) {
//        is Resource -> this
//        is Attr -> Resource(
//            theme.getAttributeResourceId(enter),
//            theme.getAttributeResourceId(exit)
//        )
//        is Composable -> this
//        is ComposableTransition -> fallback.asResource(theme)
//    }
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
): NavigationAnimation.Resource {
    val animationScale = runCatching {
        Settings.Global.getFloat(context.activity.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE)
    }.getOrDefault(1.0f)

    if(animationScale < 0.01f) {
        return NavigationAnimation.Resource(0, 0)
    }
    if (navigationInstruction is NavigationInstruction.Open<*> && navigationInstruction.children.isNotEmpty()) {
        return NavigationAnimation.Resource(0, 0)
    }

    if (navigationInstruction is NavigationInstruction.Open<*> && context.contextReference is AbstractSingleFragmentActivity) {
        val singleFragmentKey = context.getNavigationHandleViewModel().key as AbstractSingleFragmentKey
        if (navigationInstruction.instructionId == singleFragmentKey.instruction.instructionId) {
            return NavigationAnimation.Resource(0, 0)
        }
    }

    if (navigationInstruction is NavigationInstruction.Open<*> && context.contextReference is AbstractComposeFragmentHost) {
        val composeHostKey = context.getNavigationHandleViewModel().key as AbstractComposeFragmentHostKey
        if (navigationInstruction.instructionId == composeHostKey.instruction.instructionId) {
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
): NavigationAnimation.Resource {
    val theme = context.activity.theme

    val instructionForAnimation =  when (
        val navigationKey = navigationInstruction.navigationKey
    ) {
        is AbstractComposeFragmentHostKey -> navigationKey.instruction
        else -> navigationInstruction
    }

    val executor = context.activity.application.navigationController.executorForOpen(
        context,
        instructionForAnimation
    )
    return executor.executor.animation(navigationInstruction).asResource(theme)
}

private fun animationsForClose(
    context: NavigationContext<*>
): NavigationAnimation.Resource {
    val theme = context.activity.theme

    val contextForAnimation = when (context.contextReference) {
        is AbstractComposeFragmentHost -> {
            context.containerManager.containers
                .firstOrNull()
                ?.activeContext
                ?: context
        }
        else -> context
    }

    val executor = context.activity.application.navigationController.executorForClose(contextForAnimation)
    return executor.closeAnimation(context).asResource(theme)
}