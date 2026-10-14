package dev.enro.html

import kotlin.test.Test
import kotlin.test.assertEquals

class LongestTransitionMillisTests {

    @Test
    fun noTransition() {
        assertEquals(0.0, longestTransitionMillis(durations = "0s", delays = "0s"))
    }

    @Test
    fun secondsAndMillisecondsAreBothRead() {
        assertEquals(250.0, longestTransitionMillis(durations = "0.2s, 250ms", delays = "0s"))
    }

    @Test
    fun delaysAreAddedAndRepeatWhenShorterThanDurations() {
        assertEquals(400.0, longestTransitionMillis(durations = "100ms, 300ms, 50ms", delays = "100ms"))
    }
}
