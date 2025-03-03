package dev.enro.test.application.compose.results

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.enro.test.application.SelectDestinationRobot
import dev.enro.tests.application.TestActivity
import org.junit.Ignore
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
            .finish()
            .assertResult("in-> in a-> in b")
    }

    @Test
    @Ignore("""
        This works on devices, but fails during tests. It appears that the ComposeRule freezes when the activity is closed,
         and doesn't automatically update as it should, so when the activity returns a result and attempts to finish,
         the activity doesn't actually finish like it should.
    """)
    fun testActivity() {

        SelectDestinationRobot(composeRule)
            .openComposeEmbeddedResultFlow()
            .getRoot()
            .pushActivity()
            .pushActivityX()
            .pushActivityY()
            .finish()
            .assertResult("act-> act x-> act y")
    }

    @Test
    fun testOutside() {
        SelectDestinationRobot(composeRule)
            .openComposeEmbeddedResultFlow()
            .getRoot()
            .pushOutside()
            .pushOutside2()
            .pushOutside1()
            .finish()
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
            .finish()
            .assertResult("in-> in b-> in a-> out 2-> out 1-> act x")
    }
}