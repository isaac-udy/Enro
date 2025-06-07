package dev.enro.tests.application.compose.results

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.enro.tests.application.SelectDestinationRobot
import dev.enro.tests.application.TestActivity
import org.junit.Rule
import org.junit.Test

class ResultsWithExtraTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<TestActivity>()

    @Test
    fun test() {
        SelectDestinationRobot(composeRule)
            .openResultsWithMetadata()
            .assertResultIs("<No Result>")
            .requestResult_B()
            .sendResult()
            .assertResultIs("B")
            .requestResult_A()
            .sendResult()
            .assertResultIs("A")
            .requestResult_C()
            .sendResult()
            .assertResultIs("C")
    }
}