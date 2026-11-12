package dev.enro.tests.application.compose

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.enro.tests.application.SelectDestinationRobot
import dev.enro.tests.application.TestActivity
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SyntheticViewModelAccessTests {
    @get:Rule
    val composeRule = createAndroidComposeRule<TestActivity>()

    @Test
    fun testValidViewModelAccess() {
        SelectDestinationRobot(composeRule)
            .openSyntheticViewModelAccess()
            .assertViewModelAccessed(0)
            .accessValidViewModel()
            .assertViewModelAccessed(1)
            .accessValidViewModel()
            .assertViewModelAccessed(2)
    }

    // Ignored: the Compose rule throws the IllegalStateException on a thread other than
    // the test thread, so JUnit does not catch the expected exception and the test fails.
    @Ignore
    @Test(expected = IllegalStateException::class)
    fun testInvalidViewModelAccess() {
        kotlin.runCatching {
            SelectDestinationRobot(composeRule)
                .openSyntheticViewModelAccess()
                .assertViewModelAccessed(0)
                .accessInvalidViewModel()
        }
    }
}