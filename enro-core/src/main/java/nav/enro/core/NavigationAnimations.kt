package nav.enro.core

import android.content.res.Resources
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlinx.android.parcel.Parcelize
import nav.enro.core.context.NavigationContext
import nav.enro.core.context.activity
import nav.enro.core.context.navigationContext
import nav.enro.core.internal.getAttributeResourceId
import nav.enro.core.navigator.toResource

sealed class NavigationAnimations : Parcelable {
    @Parcelize
    data class Resource(
        val openEnter: Int,
        val openExit: Int,
        val closeEnter: Int,
        val closeExit: Int
    ) : NavigationAnimations()

    @Parcelize
    data class Attr(
        val openEnter: Int,
        val openExit: Int,
        val closeEnter: Int,
        val closeExit: Int
    ) : NavigationAnimations()

    companion object {
        val default = Attr(
            openEnter = android.R.attr.activityOpenEnterAnimation,
            openExit = android.R.attr.activityOpenExitAnimation,
            closeEnter = android.R.attr.activityCloseEnterAnimation,
            closeExit = android.R.attr.activityCloseExitAnimation
        )

        val none = Resource(0, R.anim.enro_no_op_animation, 0, 0)
    }
}

fun NavigationAnimations.toResource(theme: Resources.Theme): NavigationAnimations.Resource =
    when (this) {
        is NavigationAnimations.Resource -> this
        is NavigationAnimations.Attr -> NavigationAnimations.Resource(
            openEnter = theme.getAttributeResourceId(openEnter),
            openExit = theme.getAttributeResourceId(openExit),
            closeEnter = theme.getAttributeResourceId(closeEnter),
            closeExit = theme.getAttributeResourceId(closeExit)
        )
    }


data class AnimationPair(
    val enter: Int,
    val exit: Int
)

fun animationsFor(
    context: FragmentActivity,
    navigationInstruction: NavigationInstruction
): AnimationPair = animationsFor(context.navigationContext, navigationInstruction)

fun animationsFor(context: Fragment, navigationInstruction: NavigationInstruction): AnimationPair =
    animationsFor(context.navigationContext, navigationInstruction)

fun animationsFor(
    context: NavigationContext<*, *>,
    navigationInstruction: NavigationInstruction
): AnimationPair {
    if (navigationInstruction is NavigationInstruction.Open<*> && navigationInstruction.children.isNotEmpty()) {
        return AnimationPair(0, 0)
    }

    return when (navigationInstruction) {
        is NavigationInstruction.Open<*> -> animationsForOpen(context, navigationInstruction)
        is NavigationInstruction.Close -> animationsForClose(context, navigationInstruction)
    }
}

private fun animationsForOpen(
    context: NavigationContext<*, *>,
    navigationInstruction: NavigationInstruction.Open<*>
): AnimationPair {
    val theme = context.activity.theme
    val navigator = context.navigator

    val navigatorAnimations = navigator.animations.toResource(theme)
    val instructionAnimations = navigationInstruction.animations?.toResource(theme)

    return when {
        instructionAnimations != null -> AnimationPair(
            instructionAnimations.openEnter,
            instructionAnimations.openExit
        )
        else -> when (navigationInstruction.navigationDirection) {
            NavigationDirection.FORWARD -> AnimationPair(
                navigatorAnimations.forwardEnter,
                navigatorAnimations.forwardExit
            )
            NavigationDirection.REPLACE -> AnimationPair(
                navigatorAnimations.replaceEnter,
                navigatorAnimations.replaceExit
            )
            NavigationDirection.REPLACE_ROOT -> AnimationPair(
                navigatorAnimations.replaceRootEnter,
                navigatorAnimations.replaceRootExit
            )
        }
    }
}

private fun animationsForClose(
    context: NavigationContext<*, *>,
    navigationInstruction: NavigationInstruction.Close
): AnimationPair {
    val theme = context.activity.theme
    val navigator = context.navigator

    val navigatorAnimations = navigator.animations.toResource(theme)
    val animations = context.instruction?.animations?.toResource(theme)

    return when {
        animations != null -> AnimationPair(
            animations.closeEnter,
            animations.closeExit
        )
        else -> AnimationPair(navigatorAnimations.closeEnter, navigatorAnimations.closeExit)
    }
}