package nav.enro.core.navigator

import android.R
import android.app.Activity
import android.content.res.Resources
import android.util.TypedValue
import android.view.ContextThemeWrapper
import androidx.fragment.app.FragmentActivity
import nav.enro.core.NavigationDirection
import nav.enro.core.NavigationInstruction

sealed class NavigatorAnimations {
    data class Resource(
        val forwardEnter: Int,
        val forwardExit: Int,

        val closeEnter: Int,
        val closeExit: Int,

        val replaceEnter: Int = forwardEnter,
        val replaceExit: Int = forwardExit,

        val replaceRootEnter: Int = replaceEnter,
        val replaceRootExit: Int = replaceExit
    ): NavigatorAnimations()

    data class Attr(
        val forwardEnter: Int,
        val forwardExit: Int,

        val closeEnter: Int,
        val closeExit: Int,

        val replaceEnter: Int = forwardEnter,
        val replaceExit: Int = forwardExit,

        val replaceRootEnter: Int = replaceEnter,
        val replaceRootExit: Int = replaceExit
    ): NavigatorAnimations()
    companion object {
        val defaultOverride = Attr(
            forwardEnter = R.attr.activityOpenEnterAnimation,
            forwardExit = R.attr.activityOpenExitAnimation,

            replaceEnter = R.attr.activityOpenEnterAnimation,
            replaceExit = R.attr.activityOpenExitAnimation,

            replaceRootEnter = R.attr.taskOpenEnterAnimation,
            replaceRootExit = R.attr.taskOpenExitAnimation,

            closeEnter = R.attr.activityCloseEnterAnimation,
            closeExit = R.attr.activityCloseExitAnimation
        )
    }
}

data class AnimationPair(
    val enter: Int,
    val exit: Int
)

fun Navigator<*, *>.animationsFor(theme: Resources.Theme, navigationInstruction: NavigationInstruction): AnimationPair {
    if(navigationInstruction is NavigationInstruction.Open<*> && navigationInstruction.children.isNotEmpty()) {
        return AnimationPair(0, 0)
    }

    val animations = animations.toResource(theme)
    return when(navigationInstruction) {
        is NavigationInstruction.Open<*> -> when(navigationInstruction.navigationDirection) {
            NavigationDirection.FORWARD -> AnimationPair(animations.forwardEnter, animations.forwardExit)
            NavigationDirection.REPLACE -> AnimationPair(animations.replaceEnter, animations.replaceExit)
            NavigationDirection.REPLACE_ROOT -> AnimationPair(animations.replaceRootEnter, animations.replaceRootExit)
        }
        NavigationInstruction.Close -> AnimationPair(animations.closeEnter, animations.closeExit)
    }
}

fun NavigatorAnimations.toResource(theme: Resources.Theme): NavigatorAnimations.Resource =
    when(this) {
        is NavigatorAnimations.Resource -> this
        is NavigatorAnimations.Attr -> NavigatorAnimations.Resource(
            forwardEnter = theme.getAttributeResourceId(forwardEnter),
            forwardExit = theme.getAttributeResourceId(forwardExit),
            closeEnter = theme.getAttributeResourceId(closeEnter),
            closeExit = theme.getAttributeResourceId(closeExit),
            replaceEnter = theme.getAttributeResourceId(replaceEnter),
            replaceExit = theme.getAttributeResourceId(replaceExit),
            replaceRootEnter = theme.getAttributeResourceId(replaceRootEnter),
            replaceRootExit = theme.getAttributeResourceId(replaceRootExit)
        )
    }

private fun  Resources.Theme.getAttributeResourceId(attr: Int) = TypedValue().let {
    resolveAttribute(attr, it, true)
    it.resourceId
}