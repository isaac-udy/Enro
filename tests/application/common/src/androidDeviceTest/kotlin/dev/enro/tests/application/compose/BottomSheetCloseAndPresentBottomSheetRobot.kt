package dev.enro.tests.application.compose

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.performClick
import dev.enro.tests.application.waitForNavigationHandle

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
            it.key is BottomSheetCloseAndPresent.BottomSheet && it.instance.id != navigation.instance.id
        }
        return BottomSheetCloseAndPresentBottomSheetRobot(composeRule)
    }
}