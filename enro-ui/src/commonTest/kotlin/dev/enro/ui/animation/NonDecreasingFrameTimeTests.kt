package dev.enro.ui.animation

import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals

class NonDecreasingFrameTimeTests {

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
        val observed = withContext(ScriptedFrameClock(listOf(100L, 200L, 150L, 300L))) {
            frameTime.observeFrames(4)
        }
        assertEquals(listOf(100L, 200L, 200L, 300L), observed)
    }

    @Test
    fun theClampIsSharedAcrossSeparateInvocations() = runTest {
        val frameTime = NonDecreasingFrameTime()
        // The two calls model two phases of one transition, such as a predictive back seek
        // followed by the settle animation.
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
