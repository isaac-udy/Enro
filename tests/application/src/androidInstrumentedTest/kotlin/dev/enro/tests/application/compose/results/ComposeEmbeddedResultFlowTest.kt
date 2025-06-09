package dev.enro.tests.application.compose.results

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.enro.tests.application.SelectDestinationRobot
import dev.enro.tests.application.TestActivity
import org.junit.Rule
import org.junit.Test

class ComposeEmbeddedResultFlowTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<TestActivity>()

    @Test
    fun testInside() {
        SelectDestinationRobot(composeRule)
            .openComposeEmbeddedResultFlow()
            .getRoot()
            .pushInside()
            .pushInsideA()
            .pushInsideB()
            .complete()
            .assertResult("in-> in a-> in b")
    }

    @Test
    fun testActivity() {

        SelectDestinationRobot(composeRule)
            .openComposeEmbeddedResultFlow()
            .getRoot()
            .pushActivity()
            .complete()
            .assertResult("act")
    }

    @Test
    fun testOutside() {
        SelectDestinationRobot(composeRule)
            .openComposeEmbeddedResultFlow()
            .getRoot()
            .pushOutside()
            .pushOutside2()
            .pushOutside1()
            .complete()
            .assertResult("out-> out 2-> out 1")
    }

    @Test
    fun testMixed() {
        SelectDestinationRobot(composeRule)
            .openComposeEmbeddedResultFlow()
            .getRoot()
            .pushInside()
            .pushInsideB()
            .pushInsideA()
            .pushOutside2()
            .pushOutside1()
            .pushActivityX() // we can only really do one activity result in the test, for the same reason the testActivity test is ignored
            .complete()
            .assertResult("in-> in b-> in a-> out 2-> out 1-> act x")
    }
}