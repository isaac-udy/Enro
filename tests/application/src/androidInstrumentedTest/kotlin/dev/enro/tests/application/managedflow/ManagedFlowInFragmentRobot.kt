package dev.enro.tests.application.managedflow

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import dev.enro.tests.application.waitForNavigationHandle
import dev.enro.tests.application.managedflow.ManagedFlowInFragment

class ManagedFlowInFragmentRobot(
    private val composeRule: ComposeTestRule
) {
    init {
        composeRule.waitForNavigationHandle {
            it.key is ManagedFlowInFragment
        }
    }

    fun getUserInformationFlow(): UserInformationRobot {
        return UserInformationRobot(composeRule)
    }

    fun getResultFragment(): ResultFragmentRobot {
        return ResultFragmentRobot(composeRule)
    }

    class ResultFragmentRobot(
        private val composeRule: ComposeTestRule
    ) {
        init {
            composeRule.waitForNavigationHandle {
                it.key is ManagedFlowInFragment.ResultFragment
            }
        }

        fun assertUserInformationDisplayed(
            name: String,
            email: String,
            age: String
        ): ResultFragmentRobot {
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