package nav.enro.core.navigator

import android.R
import android.app.Activity
import android.util.TypedValue
import androidx.fragment.app.FragmentActivity
import nav.enro.core.NavigationDirection

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

fun NavigatorAnimations.Resource.animationsForOpen(navigationDirection: NavigationDirection): Pair<Int, Int> {
    return when(navigationDirection) {
        NavigationDirection.FORWARD -> forwardEnter to forwardExit
        NavigationDirection.REPLACE -> replaceEnter to replaceExit
        NavigationDirection.REPLACE_ROOT -> replaceRootEnter to replaceRootExit
    }
}

fun NavigatorAnimations.Resource.animationsForClose(): Pair<Int, Int> {
    return closeEnter to closeExit
}

fun NavigatorAnimations.toResource(activity: FragmentActivity): NavigatorAnimations.Resource =
    when(this) {
        is NavigatorAnimations.Resource -> this
        is NavigatorAnimations.Attr -> NavigatorAnimations.Resource(
            forwardEnter = activity.getAttributeResourceId(forwardEnter),
            forwardExit = activity.getAttributeResourceId(forwardExit),
            closeEnter = activity.getAttributeResourceId(closeEnter),
            closeExit = activity.getAttributeResourceId(closeExit),
            replaceEnter = activity.getAttributeResourceId(replaceEnter),
            replaceExit = activity.getAttributeResourceId(replaceExit),
            replaceRootEnter = activity.getAttributeResourceId(replaceRootEnter),
            replaceRootExit = activity.getAttributeResourceId(replaceRootExit)
        )
    }

private fun Activity.getAttributeResourceId(attr: Int) = TypedValue().let {
    theme.resolveAttribute(attr, it, true)
    it.resourceId
}