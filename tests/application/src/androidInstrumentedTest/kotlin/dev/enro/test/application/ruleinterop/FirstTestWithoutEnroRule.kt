package dev.enro.test.application.ruleinterop

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.enro.test.application.SelectDestinationRobot
import dev.enro.tests.application.TestActivity
import org.junit.Rule
import org.junit.Test

class FirstTestWithoutEnroRule() {
    @get:Rule
    val composeRule = createAndroidComposeRule<TestActivity>()

    @Test
    fun test() {
        SelectDestinationRobot(composeRule)
            .openSimpleActivity()
            .close()
    }
}