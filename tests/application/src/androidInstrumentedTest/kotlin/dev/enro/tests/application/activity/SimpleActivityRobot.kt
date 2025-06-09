package dev.enro.tests.application.activity

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.performClick
import dev.enro.context.RootContext
import dev.enro.tests.application.waitForNavigationContext

class SimpleActivityRobot(
    private val composeRule: ComposeTestRule
) {

    init {
        composeRule.waitForNavigationContext {
            it is RootContext && it.parent is SimpleActivityImpl
        }
    }

    fun close() {
        composeRule.onNode(hasText("Close Activity"))
            .performClick()
    }
}