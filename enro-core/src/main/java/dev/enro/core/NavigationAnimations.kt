package dev.enro.core

import android.content.res.Resources
import android.os.Parcelable
import dev.enro.core.compose.ComposeFragmentHost
import dev.enro.core.compose.ComposeFragmentHostKey
import dev.enro.core.controller.navigationController
import dev.enro.core.fragment.internal.AbstractSingleFragmentActivity
import dev.enro.core.fragment.internal.SingleFragmentKey
import dev.enro.core.internal.getAttributeResourceId
import kotlinx.parcelize.Parcelize

sealed class AnimationPair : Parcelable {
    abstract val enter: Int
    abstract val exit: Int

    @Parcelize
    class Resource(
        override val enter: Int,
        override val exit: Int
    ) : AnimationPair()

    @Parcelize
    class Attr(
        override val enter: Int,
        override val exit: Int
    ) : AnimationPair()

    fun asResource(theme: Resources.Theme) = when (this) {
        is Resource -> this
        is Attr -> Resource(
            theme.getAttributeResourceId(enter),
            theme.getAttributeResourceId(exit)
        )
    }
}

object DefaultAnimations {
    val forward = AnimationPair.Attr(
        enter = android.R.attr.activityOpenEnterAnimation,
        exit = android.R.attr.activityOpenExitAnimation
    )

    val replace = AnimationPair.Attr(
        enter = android.R.attr.activityOpenEnterAnimation,
        exit = android.R.attr.activityOpenExitAnimation
    )

    val replaceRoot = AnimationPair.Attr(
        enter = android.R.attr.taskOpenEnterAnimation,
        exit = android.R.attr.taskOpenExitAnimation
    )

    val close = AnimationPair.Attr(
        enter = android.R.attr.activityCloseEnterAnimation,
        exit = android.R.attr.activityCloseExitAnimation
    )

    val none = AnimationPair.Resource(
        enter = 0,
        exit = R.anim.enro_no_op_animation
    )
}

fun animationsFor(
    context: NavigationContext<*>,
    navigationInstruction: NavigationInstruction
): AnimationPair.Resource {
    if (navigationInstruction is NavigationInstruction.Open && navigationInstruction.children.isNotEmpty()) {
        return AnimationPair.Resource(0, 0)
    }

    if (navigationInstruction is NavigationInstruction.Open && context.contextReference is AbstractSingleFragmentActivity) {
        val singleFragmentKey = context.getNavigationHandleViewModel().key as SingleFragmentKey
        if (navigationInstruction.instructionId == singleFragmentKey.instruction.instructionId) {
            return AnimationPair.Resource(0, 0)
        }
    }

    if (navigationInstruction is NavigationInstruction.Open && context.contextReference is ComposeFragmentHost) {
        val composeHostKey = context.getNavigationHandleViewModel().key as ComposeFragmentHostKey
        if (navigationInstruction.instructionId == composeHostKey.instruction.instructionId) {
            return AnimationPair.Resource(0, 0)
        }
    }

    return when (navigationInstruction) {
        is NavigationInstruction.Open -> animationsForOpen(context, navigationInstruction)
        is NavigationInstruction.Close -> animationsForClose(context)
    }
}

private fun animationsForOpen(
    context: NavigationContext<*>,
    navigationInstruction: NavigationInstruction.Open
): AnimationPair.Resource {
    val theme = context.activity.theme
    val executor = context.activity.application.navigationController.executorForOpen(
        context,
        navigationInstruction
    )
    return executor.executor.animation(navigationInstruction).asResource(theme)
}

private fun animationsForClose(
    context: NavigationContext<*>
): AnimationPair.Resource {
    val theme = context.activity.theme
    val executor = context.activity.application.navigationController.executorForClose(context)
    return executor.closeAnimation(context).asResource(theme)
}