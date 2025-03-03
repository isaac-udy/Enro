package dev.enro.test.application.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import dev.enro.test.application.waitForNavigationHandle
import dev.enro.tests.application.compose.BottomSheetChangeSize

class BottomSheetChangeSizeBottomSheetRobot(
    private val composeRule: ComposeTestRule
) {

    private val navigation = composeRule.waitForNavigationHandle {
        it.key is BottomSheetChangeSize.BottomSheet
    }

    fun assertBottomSheetIsVisible() {
        composeRule.onNode(hasText("BottomSheet"))
            .assertIsDisplayed()

        composeRule.onNode(hasText("Item 0"))
            .assertIsDisplayed()

        composeRule.onNode(hasText("Item 1"))
            .assertIsDisplayed()
    }
}