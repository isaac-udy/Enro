package nav.enro.core.internal.executors.override

import androidx.fragment.app.FragmentActivity
import nav.enro.core.internal.getAttributeResourceId

sealed class AnimationOverride {
    data class Resource(
        val forwardEnter: Int,
        val forwardExit: Int,
        val forwardCloseEnter: Int,
        val forwardCloseExit: Int,

        val replaceEnter: Int = forwardEnter,
        val replaceExit: Int = forwardExit,
        val replaceCloseEnter: Int = forwardCloseEnter,
        val replaceCloseExit: Int = forwardCloseExit,

        val replaceRootEnter: Int = replaceEnter,
        val replaceRootExit: Int = replaceExit,
        val replaceRootCloseEnter: Int = replaceCloseEnter,
        val replaceRootCloseExit: Int = replaceCloseExit
    ): AnimationOverride()

    data class Attr(
        val forwardEnter: Int,
        val forwardExit: Int,
        val forwardCloseEnter: Int,
        val forwardCloseExit: Int,

        val replaceEnter: Int = forwardEnter,
        val replaceExit: Int = forwardExit,
        val replaceCloseEnter: Int = forwardCloseEnter,
        val replaceCloseExit: Int = forwardCloseExit,

        val replaceRootEnter: Int = replaceEnter,
        val replaceRootExit: Int = replaceExit,
        val replaceRootCloseEnter: Int = replaceCloseEnter,
        val replaceRootCloseExit: Int = replaceCloseExit
    ): AnimationOverride() {
        companion object {
            val defaultOverride = Attr(
                forwardEnter = android.R.attr.activityOpenEnterAnimation,
                forwardExit = android.R.attr.activityOpenExitAnimation,
                forwardCloseEnter = android.R.attr.activityCloseEnterAnimation,
                forwardCloseExit = android.R.attr.activityCloseExitAnimation,

                replaceEnter = android.R.attr.activityOpenEnterAnimation,
                replaceExit = android.R.attr.activityOpenExitAnimation,
                replaceCloseEnter = android.R.attr.activityCloseEnterAnimation,
                replaceCloseExit = android.R.attr.activityCloseExitAnimation,

                replaceRootEnter = android.R.attr.taskOpenEnterAnimation,
                replaceRootExit = android.R.attr.taskOpenExitAnimation,
                replaceRootCloseEnter = android.R.attr.taskCloseEnterAnimation,
                replaceRootCloseExit = android.R.attr.taskCloseExitAnimation
            )
        }
    }

    fun toResource(activity: FragmentActivity): Resource =
        when(this) {
            is Resource -> this
            is Attr -> Resource(
                forwardEnter = activity.getAttributeResourceId(forwardEnter),
                forwardExit = activity.getAttributeResourceId(forwardExit),
                forwardCloseEnter = activity.getAttributeResourceId(forwardCloseEnter),
                forwardCloseExit = activity.getAttributeResourceId(forwardCloseExit),
                replaceEnter = activity.getAttributeResourceId(replaceEnter),
                replaceExit = activity.getAttributeResourceId(replaceExit),
                replaceCloseEnter = activity.getAttributeResourceId(replaceCloseEnter),
                replaceCloseExit = activity.getAttributeResourceId(replaceCloseExit),
                replaceRootEnter = activity.getAttributeResourceId(replaceRootEnter),
                replaceRootExit = activity.getAttributeResourceId(replaceRootExit),
                replaceRootCloseEnter = activity.getAttributeResourceId(replaceRootCloseEnter),
                replaceRootCloseExit = activity.getAttributeResourceId(replaceRootCloseExit)
            )
        }
}
