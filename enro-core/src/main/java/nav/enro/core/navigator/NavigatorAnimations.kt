package nav.enro.core.navigator

import android.content.res.Resources
import nav.enro.core.internal.getAttributeResourceId

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
            forwardEnter = android.R.attr.activityOpenEnterAnimation,
            forwardExit = android.R.attr.activityOpenExitAnimation,

            replaceEnter = android.R.attr.activityOpenEnterAnimation,
            replaceExit = android.R.attr.activityOpenExitAnimation,

            replaceRootEnter = android.R.attr.taskOpenEnterAnimation,
            replaceRootExit = android.R.attr.taskOpenExitAnimation,

            closeEnter = android.R.attr.activityCloseEnterAnimation,
            closeExit = android.R.attr.activityCloseExitAnimation
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
