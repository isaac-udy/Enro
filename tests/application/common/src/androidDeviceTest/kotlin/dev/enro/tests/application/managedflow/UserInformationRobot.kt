@file:OptIn(ExperimentalTestApi::class)

package dev.enro.tests.application.managedflow

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import dev.enro.tests.application.waitForNavigationHandle
import dev.enro.tests.application.managedflow.UserInformationFlow

class UserInformationRobot(
    private val composeRule: ComposeTestRule
) {
    init {
        composeRule.waitForNavigationHandle {
            it.key is UserInformationFlow
        }
    }

    fun enterName(
        name: String
    ): UserInformationRobot {
        composeRule.waitUntilAtLeastOneExists(hasText("What's your name?"))
        composeRule.onNodeWithTag("UserInformationFlow.GetName.TextField")
            .performTextReplacement(name)
        return this
    }

    fun enterEmail(
        email: String
    ): UserInformationRobot {
        composeRule.waitUntilAtLeastOneExists(hasText("What's your email?"))
        composeRule.onNodeWithTag("UserInformationFlow.GetEmail.TextField")
            .performTextReplacement(email)
        return this
    }

    fun enterAge(
        age: String
    ): UserInformationRobot {
        composeRule.waitUntilAtLeastOneExists(hasText("How old are you?"))
        composeRule.onNodeWithTag("UserInformationFlow.GetAge.TextField")
            .performTextReplacement(age)
        return this
    }

    fun dismissErrorDialog(): UserInformationRobot {
        composeRule.waitUntilAtLeastOneExists(hasTestTag("UserInformationFlow.ErrorDialog.OK"))
        composeRule.onNodeWithTag("UserInformationFlow.ErrorDialog.OK")
            .performClick()
        return this
    }

    fun continueToNextStep(): UserInformationRobot {
        composeRule.onNodeWithText("Continue")
            .performClick()
        return this
    }
}