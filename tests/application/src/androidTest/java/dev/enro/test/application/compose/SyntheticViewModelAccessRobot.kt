package dev.enro.test.application.compose

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.enro.test.application.waitForNavigationHandle
import dev.enro.tests.application.compose.SyntheticViewModelAccess

class SyntheticViewModelAccessRobot(
    private val composeRule: ComposeTestRule
) {
    init {
        composeRule.waitForNavigationHandle {
            it.key is SyntheticViewModelAccess
        }
    }

    fun accessValidViewModel(): SyntheticViewModelAccessRobot {
        composeRule.onNodeWithText("Access Valid ViewModel")
            .performClick()
        return this
    }

    fun accessInvalidViewModel(): SyntheticViewModelAccessRobot {
        composeRule.onNodeWithText("Access Invalid ViewModel (throws)")
            .performClick()
        return this
    }

    fun assertViewModelAccessed(times: Int): SyntheticViewModelAccessRobot {
        composeRule.onNodeWithText("ViewModel Accessed $times times")
            .assertExists()
        return this
    }
}
