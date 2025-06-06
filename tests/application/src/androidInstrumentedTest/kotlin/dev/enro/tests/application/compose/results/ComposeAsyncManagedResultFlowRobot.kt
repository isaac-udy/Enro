package dev.enro.tests.application.compose.results

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import dev.enro.context.requireViewModel
import dev.enro.tests.application.waitForNavigationContext
import org.junit.Assert.assertTrue

@OptIn(ExperimentalTestApi::class)
class ComposeAsyncManagedResultFlowRobot(
    private val composeRule: ComposeTestRule
) {
    // Increased maximum timeout because the async actions on the screen can take up to 2_500 ms
    private val maximumTimeout = 5_000L

    val viewModel = composeRule
        .waitForNavigationContext<ComposeAsyncManagedResultFlow>()
        .requireViewModel<ComposeAsyncManagedResultViewModel>()

    fun assertStepOne(): ComposeAsyncManagedResultFlowRobot {
        composeRule.waitUntilAtLeastOneExists(hasText("Step: One"), maximumTimeout)
        assertTrue(viewModel.state.value.initialData is AsyncData.Loaded)
        composeRule.onNodeWithText("Step: One")
            .assertExists()
        return this
    }

    fun assertStepTwo(): ComposeAsyncManagedResultFlowRobot {
        composeRule.waitUntilAtLeastOneExists(hasText("Step: Two"), maximumTimeout)
        assertTrue(viewModel.state.value.dataAfterStepOne is AsyncData.Loaded<*>)
        composeRule.onNodeWithText("Step: Two")
            .assertExists()
        composeRule.onNodeWithText("Extra: ${ComposeAsyncManagedResultFlow.hashCode()}")
            .assertExists()
        return this
    }

    fun assertFinalStep(): ComposeAsyncManagedResultFlowRobot {
        composeRule.waitUntilAtLeastOneExists(hasText("Final Screen"), maximumTimeout)
        assertTrue(viewModel.state.value.dataAfterStepTwo is AsyncData.Loaded<*>)
        composeRule.onNodeWithText("Final Screen")
            .assertExists()
        return this
    }

    fun navigateBack(): ComposeAsyncManagedResultFlowRobot {
        Espresso.pressBack()
        return this
    }

    fun continueStepOneA(): ComposeAsyncManagedResultFlowRobot {
        composeRule.waitUntilAtLeastOneExists(hasText("Step: One"), maximumTimeout)
        composeRule.onNodeWithText("Continue (A)")
            .performClick()
        return this
    }

    fun continueStepOneB(): ComposeAsyncManagedResultFlowRobot {
        composeRule.waitUntilAtLeastOneExists(hasText("Step: One"), maximumTimeout)
        composeRule.onNodeWithText("Continue (B)")
            .performClick()
        return this
    }

    fun continueStepTwoA(): ComposeAsyncManagedResultFlowRobot {
        composeRule.waitUntilAtLeastOneExists(hasText("Step: Two"), maximumTimeout)
        composeRule.onNodeWithText("Continue (A)")
            .performClick()
        return this
    }

    fun continueStepTwoB(): ComposeAsyncManagedResultFlowRobot {
        composeRule.waitUntilAtLeastOneExists(hasText("Step: Two"), maximumTimeout)
        composeRule.onNodeWithText("Continue (B)")
            .performClick()
        return this
    }

    fun finish(): ComposeAsyncManagedResultFlowRobot {
        composeRule.waitUntilAtLeastOneExists(hasText("Final Screen"), maximumTimeout)
        composeRule.onNodeWithText("Finish")
            .performClick()
        return this
    }

}