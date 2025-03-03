package dev.enro.test.application.compose

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.enro.test.application.SelectDestinationRobot
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

    // apparently this test doesn't work because the Compose rule throws an exception on
    // the wrong thread, so the expected exception is not caught and the test fails (even though we get the expected result)
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