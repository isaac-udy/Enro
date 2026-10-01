package dev.enro.ui.animation

import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals

class NonDecreasingFrameTimeTests {

    /** Hands out a scripted sequence of frame times, including regressions. */
    private class ScriptedFrameClock(private val frameTimes: List<Long>) : MonotonicFrameClock {
        private var index = 0

        override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
            return onFrame(frameTimes[index++])
        }
    }

    private suspend fun NonDecreasingFrameTime.observeFrames(count: Int): List<Long> {
        val observed = mutableListOf<Long>()
        runPreventingFrameTimeRegression {
            repeat(count) {
                withFrameNanos { frameTimeNanos -> observed.add(frameTimeNanos) }
            }
        }
        return observed
    }

    @Test
    fun monotonicFrameTimesArePassedThroughUnchanged() = runTest {
        val frameTime = NonDecreasingFrameTime()
        val observed = withContext(ScriptedFrameClock(listOf(100L, 200L, 300L))) {
            frameTime.observeFrames(3)
        }
        assertEquals(listOf(100L, 200L, 300L), observed)
    }

    @Test
    fun aFrameTimeRegressionIsClampedToTheLastSeenFrameTime() = runTest {
        val frameTime = NonDecreasingFrameTime()
        // The 150L frame goes backwards relative to 200L; it must be clamped so the animation
        // never sees a negative frame delta (the "Cannot round NaN value" crash).
        val observed = withContext(ScriptedFrameClock(listOf(100L, 200L, 150L, 300L))) {
            frameTime.observeFrames(4)
        }
        assertEquals(listOf(100L, 200L, 200L, 300L), observed)
    }

    @Test
    fun theClampIsSharedAcrossSeparateInvocations() = runTest {
        val frameTime = NonDecreasingFrameTime()
        // Two invocations model two phases of one transition (e.g. a predictive-back seek
        // handing over to the settle animation); a regression across the boundary is clamped too.
        val first = withContext(ScriptedFrameClock(listOf(100L, 200L))) {
            frameTime.observeFrames(2)
        }
        val second = withContext(ScriptedFrameClock(listOf(150L, 300L))) {
            frameTime.observeFrames(2)
        }
        assertEquals(listOf(100L, 200L), first)
        assertEquals(listOf(200L, 300L), second)
    }

    @Test
    fun runsWithoutAFrameClockInTheContext() = runTest {
        val frameTime = NonDecreasingFrameTime()
        var ran = false
        frameTime.runPreventingFrameTimeRegression { ran = true }
        assertEquals(true, ran)
    }
}
