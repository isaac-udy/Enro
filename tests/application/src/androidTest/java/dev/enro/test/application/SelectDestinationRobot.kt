package dev.enro.test.application

import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onSiblings
import androidx.compose.ui.test.performClick
import dev.enro.test.application.compose.BottomSheetCloseAndPresentRobot
import dev.enro.tests.application.SelectDestination

class SelectDestinationRobot(
    private val composeRule: ComposeTestRule
) {

    init {
        composeRule.waitForNavigationHandle {
            it.key is SelectDestination
        }
    }

    fun openBottomSheetCloseAndPresent() : BottomSheetCloseAndPresentRobot {
        composeRule.onNode(hasText("Bottom Sheet Close And Present"))
            .onSiblings()
            .filterToOne(hasText("Push"))
            .performClick()

        return BottomSheetCloseAndPresentRobot(composeRule)
    }
}