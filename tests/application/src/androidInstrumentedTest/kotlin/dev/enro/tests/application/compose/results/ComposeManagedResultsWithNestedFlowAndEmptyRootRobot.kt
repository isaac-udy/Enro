package dev.enro.tests.application.compose.results

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick

@OptIn(ExperimentalTestApi::class)
class ComposeManagedResultsWithNestedFlowAndEmptyRootRobot(
    private val composeRule: ComposeTestRule
) {

    fun assertNestedFlowIsDisplayed() : ComposeManagedResultsWithNestedFlowAndEmptyRootRobot {
        composeRule.waitUntilAtLeastOneExists(hasText("Nested Flow"))
        composeRule.onNodeWithText("Nested Flow")
            .assertExists()
        return this
    }

    fun assertOuterStepTwoIsDisplayed() : ComposeManagedResultsWithNestedFlowAndEmptyRootRobot {
        composeRule.waitUntilAtLeastOneExists(hasText("Step Two"))
        composeRule.onNodeWithText("Step Two")
            .assertExists()
        return this
    }

    fun goToNestedStepOne(): ComposeManagedResultsWithNestedFlowAndEmptyRootRobot {
        composeRule.onNodeWithText("Next (to nested step one)")
            .performClick()
        return this
    }

    fun goToNestedStepTwo(): ComposeManagedResultsWithNestedFlowAndEmptyRootRobot {
        composeRule.onNodeWithText("Next (to nested step two)")
            .performClick()
        return this
    }

    fun setResultOnNestedStepTwo(): ComposeManagedResultsWithNestedFlowAndEmptyRootRobot {
        composeRule.onNodeWithText("Sheep")
            .performClick()
        return this
    }


}