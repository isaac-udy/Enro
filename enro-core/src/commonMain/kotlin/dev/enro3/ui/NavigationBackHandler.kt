package dev.enro3.ui

import androidx.annotation.IntDef
import androidx.annotation.RestrictTo
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow


@Composable
internal expect fun NavigationBackHandler(
    enabled: Boolean = true,
    onBack: suspend (progress: Flow<NavigationBackEvent>) -> Unit
)

public class NavigationBackEvent(
    /**
     * Absolute X location of the touch point of this event in the coordinate space of the screen
     * that received this navigation event.
     */
    public val touchX: Float,
    /**
     * Absolute Y location of the touch point of this event in the coordinate space of the screen
     * that received this navigation event.
     */
    public val touchY: Float,
    /** Value between 0 and 1 on how far along the back gesture is. */
    public val progress: Float,
    /** Indicates which edge the swipe starts from. */
    public val swipeEdge: @SwipeEdge Int,
    /** Frame time of the navigation event. */
    public val frameTimeMillis: Long = 0,
) {

    /**  */
    @Target(AnnotationTarget.TYPE)
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    @Retention(AnnotationRetention.SOURCE)
    @IntDef(EDGE_LEFT, EDGE_RIGHT, EDGE_NONE)
    public annotation class SwipeEdge

    public companion object {
        /** Indicates that the edge swipe starts from the left edge of the screen */
        public const val EDGE_LEFT: Int = 0

        /** Indicates that the edge swipe starts from the right edge of the screen */
        public const val EDGE_RIGHT: Int = 1

        /**
         * Indicates that the back event was not triggered by an edge swipe back gesture. This
         * applies to cases like using the back button in 3-button navigation or pressing a hardware
         * back button.
         */
        public const val EDGE_NONE: Int = 2
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as NavigationBackEvent

        if (touchX != other.touchX) return false
        if (touchY != other.touchY) return false
        if (progress != other.progress) return false
        if (swipeEdge != other.swipeEdge) return false
        if (frameTimeMillis != other.frameTimeMillis) return false

        return true
    }

    override fun hashCode(): Int {
        var result = touchX.hashCode()
        result = 31 * result + touchY.hashCode()
        result = 31 * result + progress.hashCode()
        result = 31 * result + swipeEdge
        result = 31 * result + frameTimeMillis.hashCode()
        return result
    }

    override fun toString(): String {
        return "NavigationEvent(touchX=$touchX, touchY=$touchY, progress=$progress, swipeEdge=$swipeEdge, frameTimeMillis=$frameTimeMillis)"
    }
}
