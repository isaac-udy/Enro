package dev.enro.tests.application.compose

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.performClick
import dev.enro.tests.application.waitForNavigationHandle
import dev.enro.tests.application.compose.BottomSheetCloseAndPresent

class BottomSheetCloseAndPresentRobot(
    private val composeRule: ComposeTestRule
) {

    init {
        composeRule.waitForNavigationHandle {
            it.key is BottomSheetCloseAndPresent
        }
    }

    fun openBottomSheet(): BottomSheetCloseAndPresentBottomSheetRobot {
        composeRule.onNode(hasText("Open BottomSheet"))
            .performClick()

        return BottomSheetCloseAndPresentBottomSheetRobot(composeRule)
    }
}