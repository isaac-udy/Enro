package dev.enro.tests.application.compose.results

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.Espresso
import dev.enro.tests.application.waitForNavigationHandle

class ComposeNestedResultsRobot(
    private val composeRule: ComposeTestRule
) {
    init {
        composeRule.waitForNavigationHandle {
            it.key is ComposeNestedResults
        }
    }

    fun getReceiver() : ReceiverRobot {
        return ReceiverRobot(composeRule)
    }

    class ReceiverRobot(private val composeRule: ComposeTestRule)  {
        init {
            composeRule.waitForNavigationHandle {
                it.key is ComposeNestedResults.Receiver
            }
        }

        fun assertCurrentResult(result: String) : ReceiverRobot {
            composeRule.onNodeWithText("Current Result: $result")
                .assertExists()
            return this
        }

        fun assertSenderContainerIsVisible() : ReceiverRobot {
            composeRule.onNodeWithText("Nested Sender Container")
                .assertExists()
            return this
        }

        fun assertSenderContainerNotVisible() : ReceiverRobot {
            composeRule.onNodeWithText("Nested Sender Container")
                .assertDoesNotExist()
            return this
        }
        
        fun openNestedSenderContainer(): ReceiverRobot {
            composeRule.onNodeWithText("Open Nested Sender Container")
                .performScrollTo()
                .performClick()

            NestedSenderContainerRobot(composeRule)
            return this
        }

        fun openSender(): SenderRobot {
            composeRule.onNodeWithText("Get Result")
                .performScrollTo()
                .performClick()

            return SenderRobot(composeRule)
        }

    }

    class NestedSenderContainerRobot(private val composeRule: ComposeTestRule)  {
        init {
            composeRule.waitForNavigationHandle {
                it.key is ComposeNestedResults.NestedSenderContainer
            }
        }
    }

    class SenderRobot(private val composeRule: ComposeTestRule) {
        init {
            composeRule.waitForNavigationHandle {
                it.key is ComposeNestedResults.Sender
            }
        }

        fun assertIsInsideContainer(): SenderRobot {
            composeRule.onNodeWithText("Nested Sender Container")
                .assertExists()
            return this
        }

        fun assertIsNotInContainer(): SenderRobot {
            composeRule.onNodeWithText("Nested Sender Container")
                .assertDoesNotExist()
            return this
        }

        fun sendA(): ReceiverRobot {
            composeRule.onNodeWithText("Send A")
                .performClick()

            return ReceiverRobot(composeRule)
        }

        fun sendB(): ReceiverRobot {
            composeRule.onNodeWithText("Send B")
                .performClick()

            return ReceiverRobot(composeRule)
        }

        fun closeWithButton(): ReceiverRobot {
            composeRule.onNodeWithText("Close")
                .performClick()

            return ReceiverRobot(composeRule)
        }

        fun closeWithBackPress(): ReceiverRobot {
            Espresso.pressBack()
            return ReceiverRobot(composeRule)
        }
    }
}