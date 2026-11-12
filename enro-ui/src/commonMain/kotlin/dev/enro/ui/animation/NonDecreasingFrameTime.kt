package dev.enro.ui.animation

import androidx.compose.runtime.MonotonicFrameClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext

/**
 * Prevents a frame time that is earlier than the previous frame time.
 *
 * Some devices (observed on Huawei and Honor) deliver a Choreographer frame time earlier
 * than the previous frame time. When [androidx.compose.animation.core.SeekableTransitionState]
 * animates a transition with a total duration of zero, the negative frame delta produces a
 * NaN fraction, and the next seek throws "IllegalArgumentException: Cannot round NaN value".
 * Upstream reports: https://issuetracker.google.com/issues/540477169 and
 * https://github.com/arkivanov/Decompose/issues/1012.
 *
 * [runPreventingFrameTimeRegression] runs the block with a [MonotonicFrameClock] whose frame
 * times never decrease. The last frame time is stored on the NonDecreasingFrameTime instance,
 * so one instance also prevents a decrease between two calls, such as a predictive back seek
 * followed by the settle animation.
 */
internal class NonDecreasingFrameTime {

    private var lastFrameTimeNanos = Long.MIN_VALUE

    internal suspend fun <R> runPreventingFrameTimeRegression(
        block: suspend CoroutineScope.() -> R,
    ): R {
        val clock = currentCoroutineContext()[MonotonicFrameClock]
            ?: return coroutineScope(block)
        return withContext(NonDecreasingFrameClock(clock), block)
    }

    private inner class NonDecreasingFrameClock(
        private val delegate: MonotonicFrameClock,
    ) : MonotonicFrameClock {
        override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
            return delegate.withFrameNanos { frameTimeNanos ->
                val time = maxOf(frameTimeNanos, lastFrameTimeNanos)
                lastFrameTimeNanos = time
                onFrame(time)
            }
        }
    }
}
