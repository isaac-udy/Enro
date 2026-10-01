package dev.enro.ui.animation

import androidx.compose.runtime.MonotonicFrameClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext

/**
 * Guards animation code against the frame time going backwards.
 *
 * Some devices (observed on Huawei and Honor) occasionally deliver a Choreographer frame time
 * that is earlier than the previous frame's. [androidx.compose.animation.core.SeekableTransitionState]
 * does not tolerate this: while animating a transition whose total duration is zero (e.g. a scene
 * change with no enter/exit animations), a negative frame delta produces a NaN fraction and the
 * next seek crashes with "IllegalArgumentException: Cannot round NaN value".
 *
 * [runPreventingFrameTimeRegression] runs [block] with a [MonotonicFrameClock] that clamps frame
 * times to be non-decreasing. The clamp state is shared across every invocation on the same
 * instance, so a regression between two phases of the same transition (e.g. a predictive-back
 * seek handing over to the settle animation) is caught too.
 *
 * See https://issuetracker.google.com/issues/540477169 and
 * https://github.com/arkivanov/Decompose/issues/1012 for the upstream reports.
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
