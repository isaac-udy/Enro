package dev.enro.test.application.compose.results

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.enro.test.application.SelectDestinationRobot
import dev.enro.tests.application.TestActivity
import org.junit.Rule
import org.junit.Test

class ComposeMixedResultTypesTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<TestActivity>()

    @Test
    fun test() {
        SelectDestinationRobot(composeRule)
            .openComposeMixedResultTypes()
            .assertStringResult()
            .assertIntResult()
            .assertListStringResult()
            .assertBooleanResult()
            .assertObjectResult()
            .assertStringResult()
            .assertBooleanResult()
            .assertIntResult()
            .assertStringResult()
    }
}