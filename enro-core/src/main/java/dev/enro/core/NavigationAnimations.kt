package dev.enro.core

import android.content.res.Resources
import android.os.Parcel
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlinx.android.parcel.Parcelize
import nav.enro.core.controller.navigationController
import nav.enro.core.internal.getAttributeResourceId

sealed class AnimationPair : Parcelable{
    abstract val enter: Int
    abstract val exit: Int

    @Parcelize
    class Resource(
        override val enter: Int,
        override val exit: Int
    ): AnimationPair()

    @Parcelize
    class Attr(
        override val enter: Int,
        override val exit: Int
    ): AnimationPair()

    fun asResource(theme: Resources.Theme) = when(this) {
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
        exit =  android.R.attr.activityCloseExitAnimation
    )

    val none = AnimationPair.Resource(
        enter = 0,
        exit =  R.anim.enro_no_op_animation
    )
}

fun animationsFor(
    context: NavigationContext<*>,
    navigationInstruction: NavigationInstruction
): AnimationPair.Resource {
    if (navigationInstruction is NavigationInstruction.Open && navigationInstruction.children.isNotEmpty()) {
        return AnimationPair.Resource(0, 0)
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
    val executor = context.activity.application.navigationController.executorForOpen(context, navigationInstruction)
    return executor.executor.animation(navigationInstruction).asResource(theme)
}

private fun animationsForClose(
    context: NavigationContext<*>
): AnimationPair.Resource {
    val theme = context.activity.theme
    val executor = context.activity.application.navigationController.executorForClose(context)
    return executor.closeAnimation(context).asResource(theme)
}