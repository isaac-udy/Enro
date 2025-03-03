package dev.enro.test.application.compose

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.performClick
import dev.enro.test.application.waitForNavigationHandle
import dev.enro.tests.application.compose.BottomSheetChangeSize

class BottomSheetChangeSizeRobot(
    private val composeRule: ComposeTestRule
) {

    init {
        composeRule.waitForNavigationHandle {
            it.key is BottomSheetChangeSize
        }
    }

    fun openBottomSheet(): BottomSheetChangeSizeBottomSheetRobot {
        composeRule.onNode(hasText("Open BottomSheet"))
            .performClick()

        return BottomSheetChangeSizeBottomSheetRobot(composeRule)
    }
}