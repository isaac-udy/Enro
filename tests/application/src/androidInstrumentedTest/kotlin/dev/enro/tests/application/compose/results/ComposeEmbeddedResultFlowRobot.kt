package dev.enro.tests.application.compose.results

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.enro.tests.application.waitForNavigationHandle

class ComposeEmbeddedResultFlowRobot(
    private val composeRule: ComposeTestRule
) {
    init {
        composeRule.waitForNavigationHandle {
            it.key is ComposeEmbeddedResultFlow
        }
    }

    fun getRoot(): RootRobot {
        return RootRobot()
    }

    inner class RootRobot() {
        init {
            composeRule.waitForNavigationHandle {
                it.key is ComposeEmbeddedResultFlow.Root
            }
        }

        fun pushInside(): InsideContainer {
            composeRule.onNodeWithText("Navigate Inside Container")
                .performClick()
            return InsideContainer("in")
        }

        fun pushOutside(): OutsideContainer {
            composeRule.onNodeWithText("Navigate Outside Container")
                .performClick()

            return OutsideContainer("out")
        }

        fun pushActivity(): ActivityRobot {
            composeRule.onNodeWithText("Navigate Activity")
                .performClick()

            return ActivityRobot("act")
        }

        fun assertResult(expectedResult: String): RootRobot {
            composeRule.onNodeWithText("Last Result: $expectedResult")
                .assertExists()
            return this
        }
    }

    inner class InsideContainer(
        private val currentResult: String
    ) {
        init {
            composeRule.waitForNavigationHandle {
                it.key is ComposeEmbeddedResultFlow.InsideContainer && (it.key as ComposeEmbeddedResultFlow.InsideContainer).currentResult == currentResult
            }
        }

        fun pushInsideA(): InsideContainer {
            composeRule.onNodeWithText("Navigate Inside Container (a)")
                .performClick()
            return InsideContainer("$currentResult-> in a")
        }

        fun pushInsideB(): InsideContainer {
            composeRule.onNodeWithText("Navigate Inside Container (b)")
                .performClick()
            return InsideContainer("$currentResult-> in b")
        }

        fun pushOutside1(): OutsideContainer {
            composeRule.onNodeWithText("Navigate Outside Container (1)")
                .performClick()

            return OutsideContainer("$currentResult-> out 1")
        }

        fun pushOutside2(): OutsideContainer {
            composeRule.onNodeWithText("Navigate Outside Container (2)")
                .performClick()

            return OutsideContainer("$currentResult-> out 2")
        }

        fun pushActivityX(): ActivityRobot {
            composeRule.onNodeWithText("Navigate Activity (x)")
                .performClick()

            return ActivityRobot("$currentResult-> act x")
        }

        fun pushActivityY(): ActivityRobot {
            composeRule.onNodeWithText("Navigate Activity (y)")
                .performClick()

            return ActivityRobot("$currentResult-> act y")
        }

        fun finish(): RootRobot {
            composeRule.onNodeWithText("Finish")
                .performClick()
            return RootRobot()
        }
    }

    inner class OutsideContainer(
        private val currentResult: String
    ) {
        init {
            composeRule.waitForNavigationHandle {
                it.key is ComposeEmbeddedResultFlow.OutsideContainer && (it.key as ComposeEmbeddedResultFlow.OutsideContainer).currentResult == currentResult
            }
        }

        fun pushOutside1(): OutsideContainer {
            composeRule.onNodeWithText("Navigate Outside Container (1)")
                .performClick()

            return OutsideContainer("$currentResult-> out 1")
        }

        fun pushOutside2(): OutsideContainer {
            composeRule.onNodeWithText("Navigate Outside Container (2)")
                .performClick()

            return OutsideContainer("$currentResult-> out 2")
        }

        fun pushActivityX(): ActivityRobot {
            composeRule.onNodeWithText("Navigate Activity (x)")
                .performClick()

            return ActivityRobot("$currentResult-> act x")
        }

        fun pushActivityY(): ActivityRobot {
            composeRule.onNodeWithText("Navigate Activity (y)")
                .performClick()

            return ActivityRobot("$currentResult-> act y")
        }

        fun finish(): RootRobot {
            composeRule.onNodeWithText("Finish")
                .performClick()
            return RootRobot()
        }
    }

    inner class ActivityRobot(
        private val currentResult: String
    ) {
        init {
            composeRule.waitForNavigationHandle {
                it.key is ComposeEmbeddedResultFlow.Activity && (it.key as ComposeEmbeddedResultFlow.Activity).currentResult == currentResult
            }
        }

        fun pushActivityX(): ActivityRobot {
            composeRule.onNodeWithText("Navigate Activity (x)")
                .performClick()

            return ActivityRobot("$currentResult-> act x")
        }

        fun pushActivityY(): ActivityRobot {
            composeRule.onNodeWithText("Navigate Activity (y)")
                .performClick()

            return ActivityRobot("$currentResult-> act y")
        }

        fun finish(): RootRobot {
            composeRule.onNodeWithText("Finish")
                .performClick()
            return RootRobot()
        }
    }
}