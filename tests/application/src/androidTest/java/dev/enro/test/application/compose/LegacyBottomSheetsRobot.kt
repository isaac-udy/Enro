package dev.enro.test.application.compose

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.performClick
import dev.enro.test.application.waitForNavigationHandle
import dev.enro.tests.application.compose.LegacyBottomSheets

class LegacyBottomSheetsRobot (
    private val composeRule: ComposeTestRule
) {
    init {
        composeRule.waitForNavigationHandle {
            it.key is LegacyBottomSheets
        }
    }

    fun openBottomSheet(): BottomSheetRobot {
        composeRule.onNode(hasText("Bottom Sheet"))
            .performClick()

        return BottomSheetRobot(composeRule)
    }

    fun openBottomSheetWithSkipHalfExpanded(): BottomSheetRobot {
        composeRule.onNode(hasText("Bottom Sheet (Skip Half Expanded)"))
            .performClick()

        return BottomSheetRobot(composeRule)
    }

    class BottomSheetRobot (
        private val composeRule: ComposeTestRule
    ) {
        init {
            composeRule.waitForNavigationHandle {
                it.key is LegacyBottomSheets.BottomSheet
            }
        }

        fun closeByButton(): LegacyBottomSheetsRobot {
            composeRule.onNode(hasText("Close"))
                .performClick()

            return LegacyBottomSheetsRobot(composeRule)
        }
    }
}