package dev.enro.tests.application.compose.results

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.enro.tests.application.waitForNavigationHandle

class ResultsWithMetadataRobot(
    val composeRule: ComposeTestRule
) {
    init {
        composeRule.waitForNavigationHandle {
            it.key is ResultsWithMetadata
        }
    }

    fun requestResult_A(): SenderRobot {
        composeRule.onNodeWithText("Request (A) as Result")
            .performClick()
        return SenderRobot(composeRule)
    }

    fun requestResult_B(): SenderRobot {
        composeRule.onNodeWithText("Request (B) as Result")
            .performClick()
        return SenderRobot(composeRule)
    }

    fun requestResult_C(): SenderRobot {
        composeRule.onNodeWithText("Request (C) as Result")
            .performClick()
        return SenderRobot(composeRule)
    }

    fun assertResultIs(result: String): ResultsWithMetadataRobot {
        composeRule.onNodeWithText("Last Result was \"$result\"")
            .assertExists()
        return this
    }

    class SenderRobot(
        val composeRule: ComposeTestRule,
    ) {
        init {
            composeRule.waitForNavigationHandle {
                it.key is ResultsWithMetadata.Sender
            }
        }

        fun sendResult(): ResultsWithMetadataRobot {
            composeRule.onNodeWithText("Send Result")
                .performClick()

            return ResultsWithMetadataRobot(composeRule)
        }
    }
}