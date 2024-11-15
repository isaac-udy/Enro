package dev.enro.test.application.managedflow

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import dev.enro.test.application.waitForNavigationHandle
import dev.enro.tests.application.managedflow.ManagedFlowInComposable

class ManagedFlowInComposableRobot(
    private val composeRule: ComposeTestRule
) {
    init {
        composeRule.waitForNavigationHandle {
            it.key is ManagedFlowInComposable
        }
    }

    fun getUserInformationFlow(): UserInformationRobot {
        return UserInformationRobot(composeRule)
    }

    fun getDisplayUserInformationFlow(): DisplayUserInformationRobot {
        return DisplayUserInformationRobot(composeRule)
    }

    class DisplayUserInformationRobot(
        private val composeRule: ComposeTestRule
    ) {
        init {
            composeRule.waitForNavigationHandle {
                it.key is ManagedFlowInComposable.DisplayUserInformation
            }
        }

        fun assertUserInformationDisplayed(
            name: String,
            email: String,
            age: String
        ) : DisplayUserInformationRobot {
            composeRule.onNodeWithText("Name: $name")
                .assertExists()
            composeRule.onNodeWithText("Email: $email")
                .assertExists()
            composeRule.onNodeWithText("Age: $age")
                .assertExists()
            return this
        }
    }
}