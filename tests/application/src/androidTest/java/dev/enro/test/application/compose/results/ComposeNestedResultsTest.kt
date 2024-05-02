package dev.enro.test.application.compose.results

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.enro.test.application.SelectDestinationRobot
import dev.enro.tests.application.TestActivity
import org.junit.Rule
import org.junit.Test

class ComposeNestedResultsTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<TestActivity>()

    @Test
    fun test() {
        SelectDestinationRobot(composeRule)
            .openComposeNestedResults()
            .getReceiver()
            .assertCurrentResult("(None)")

            .openSender()
            .assertIsNotInContainer()
            .sendA()
            .assertCurrentResult("A")
            .assertSenderContainerNotVisible()

            .openSender()
            .assertIsNotInContainer()
            .closeWithButton()
            .assertCurrentResult("Closed")
            .assertSenderContainerNotVisible()

            .openSender()
            .assertIsNotInContainer()
            .sendB()
            .assertCurrentResult("B")
            .assertSenderContainerNotVisible()

            .openSender()
            .assertIsNotInContainer()
            .closeWithBackPress()
            .assertCurrentResult("Closed")
            .assertSenderContainerNotVisible()

            .openNestedSenderContainer()
            .assertSenderContainerIsVisible()
            .openSender()
            .assertIsInsideContainer()
            .sendB()
            .assertCurrentResult("B")
            .assertSenderContainerNotVisible()

            .openNestedSenderContainer()
            .assertSenderContainerIsVisible()
            .openSender()
            .assertIsInsideContainer()
            .closeWithButton()
            .assertCurrentResult("Closed")
            .assertSenderContainerNotVisible()

            .openNestedSenderContainer()
            .assertSenderContainerIsVisible()
            .openSender()
            .assertIsInsideContainer()
            .sendA()
            .assertCurrentResult("A")
            .assertSenderContainerNotVisible()

            .openNestedSenderContainer()
            .assertSenderContainerIsVisible()
            .openSender()
            .assertIsInsideContainer()
            .closeWithBackPress()
            .assertCurrentResult("Closed")
            .assertSenderContainerNotVisible()

    }
}