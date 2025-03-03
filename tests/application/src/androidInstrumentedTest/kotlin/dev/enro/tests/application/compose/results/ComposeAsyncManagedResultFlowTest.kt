package dev.enro.tests.application.compose.results

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.enro.tests.application.SelectDestinationRobot
import dev.enro.tests.application.TestActivity
import org.junit.Rule
import org.junit.Test

class ComposeAsyncManagedResultFlowTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<TestActivity>()

    @Test
    fun test() {
        SelectDestinationRobot(composeRule)
            .openComposeAsyncManagedResultFlow()
            .assertStepOne()
            .continueStepOneA()
            .assertStepTwo()
            .navigateBack()
            .assertStepOne()
            .continueStepOneB()
            .assertStepTwo()
            .continueStepTwoA()
            .assertFinalStep()
            .navigateBack()
            .navigateBack()
            .assertStepOne()
            .continueStepOneA()
            .assertStepTwo()
            .continueStepTwoB()
            .assertFinalStep()
            .finish()
    }
}