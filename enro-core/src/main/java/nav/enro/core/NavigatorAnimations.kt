package nav.enro.core

import android.R
import android.content.res.Resources

// Allow setting on NavigationDestination annotation?
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
        val default = Attr(
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
