package dev.enro.tests.application.compose.results

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso
import dev.enro.tests.application.SelectDestinationRobot
import dev.enro.tests.application.TestActivity
import org.junit.Rule
import org.junit.Test

class ComposeManagedResultFlowTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<TestActivity>()

    @Test
    fun test() {
        SelectDestinationRobot(composeRule)
            .openComposeManagedResultFlow()
            .assertFirstResultActive()
            .continueA()

            .assertPresentedResultActive()
            .continueA()
            .assertSecondResultActive()
            .pressBack()
            .assertPresentedResultActive()
            .continueB()

            .assertSecondResultActive()
            .continueA()
            .assertTransientResultActive()
            .pressBack()
            .assertSecondResultActive()
            .continueA()

            .assertTransientResultActive()
            .continueA()

            .assertThirdResultActive()
            .pressBack()
            .assertSecondResultActive()
            .continueA()
            .assertThirdResultActive()
            .pressBack()
            .assertSecondResultActive()
            .continueB()
            .assertTransientResultActive()
            .continueA()
            .assertThirdResultActive()
            .continueA()
            .assertConfirmThirdResultActive()
            .close()
            .assertThirdResultActive()
            .continueA()
            .assertConfirmThirdResultActive()
            .cont()

            .assertFinalScreenActive()
            .assertResultState(
                first = "A",
                presented = "B",
                second = "B",
                transient = "A",
                third = "A",
            )

            .editThird()
            .assertThirdResultActive()
            .continueB()
            .assertConfirmThirdResultActive()
            .cont()

            .assertFinalScreenActive()
            .assertResultState(third = "B")

            .editSecond()
            .assertSecondResultActive()
            .continueA()
            .assertTransientResultActive()
            .continueB()
            .assertFinalScreenActive()
            .assertResultState(second = "A", transient = "B")

            .editFirst()
            .assertFirstResultActive()
            .continueB()
            .assertFinalScreenActive()
            .assertResultState(first = "B")
    }

    private fun Any.pressBack(): ComposeManagedResultFlowRobot {
        Espresso.pressBack()
        return ComposeManagedResultFlowRobot(composeRule)
    }
}