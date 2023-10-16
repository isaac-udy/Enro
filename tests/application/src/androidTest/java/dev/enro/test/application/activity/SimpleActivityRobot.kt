package dev.enro.test.application.activity

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.performClick
import dev.enro.test.application.waitForNavigationHandle
import dev.enro.tests.application.activity.SimpleActivity

class SimpleActivityRobot(
    private val composeRule: ComposeTestRule
) {

    init {
        composeRule.waitForNavigationHandle {
            it.key is SimpleActivity
        }
    }

    fun close() {
        composeRule.onNode(hasText("Close Activity"))
            .performClick()
    }
}