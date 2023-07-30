package dev.enro.test.application.compose

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.performClick
import dev.enro.test.application.waitForNavigationHandle
import dev.enro.tests.application.compose.BottomSheetCloseAndPresent

class BottomSheetCloseAndPresentBottomSheetRobot(
    private val composeRule: ComposeTestRule
) {

    private val navigation = composeRule.waitForNavigationHandle {
        it.key is BottomSheetCloseAndPresent.BottomSheet
    }

    fun closeAndPresentBottomSheet(): BottomSheetCloseAndPresentBottomSheetRobot {
        composeRule.onNode(hasText("Close and Present BottomSheet"))
            .performClick()

        composeRule.waitForNavigationHandle {
            it.key is BottomSheetCloseAndPresent.BottomSheet && it.id != navigation.id
        }
        return BottomSheetCloseAndPresentBottomSheetRobot(composeRule)
    }
}