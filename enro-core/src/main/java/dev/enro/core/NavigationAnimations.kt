package dev.enro.core

import android.content.res.Resources
import android.provider.Settings
import dev.enro.core.compose.AbstractComposeFragmentHost
import dev.enro.core.compose.AbstractComposeFragmentHostKey
import dev.enro.core.controller.navigationController
import dev.enro.core.fragment.internal.AbstractSingleFragmentActivity
import dev.enro.core.fragment.internal.AbstractSingleFragmentKey
import dev.enro.core.internal.getAttributeResourceId

@Deprecated("Please use NavigationAnimation")
typealias AnimationPair = NavigationAnimation

sealed class NavigationAnimation {
    abstract val enter: Int
    abstract val exit: Int

    class Resource(
        override val enter: Int,
        override val exit: Int
    ) : NavigationAnimation()

    class Attr(
        override val enter: Int,
        override val exit: Int
    ) : NavigationAnimation()

    fun asResource(theme: Resources.Theme) = when (this) {
        is Resource -> this
        is Attr -> Resource(
            theme.getAttributeResourceId(enter),
            theme.getAttributeResourceId(exit)
        )
    }
}

object DefaultAnimations {
    val push = NavigationAnimation.Attr(
        enter = android.R.attr.activityOpenEnterAnimation,
        exit = android.R.attr.activityOpenExitAnimation
    )

    val present = NavigationAnimation.Attr(
        enter = android.R.attr.activityOpenEnterAnimation,
        exit = android.R.attr.activityOpenExitAnimation
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
    val animationScale = Settings.Global.getFloat(context.activity.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE)
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
    val executor = context.activity.application.navigationController.executorForOpen(
        context,
        navigationInstruction
    )
    return executor.executor.animation(navigationInstruction).asResource(theme)
}

private fun animationsForClose(
    context: NavigationContext<*>
): NavigationAnimation.Resource {
    val theme = context.activity.theme
    val executor = context.activity.application.navigationController.executorForClose(context)
    return executor.closeAnimation(context).asResource(theme)
}