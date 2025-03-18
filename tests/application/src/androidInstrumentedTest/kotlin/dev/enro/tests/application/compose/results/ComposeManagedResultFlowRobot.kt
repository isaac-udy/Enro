package dev.enro.tests.application.compose.results

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onSiblings
import androidx.compose.ui.test.performClick
import dev.enro.tests.application.waitForNavigationContext
import dev.enro.tests.application.compose.results.ComposeManagedResultFlow

@OptIn(ExperimentalTestApi::class)
class ComposeManagedResultFlowRobot(
    private val composeRule: ComposeTestRule
) {
    init {
        composeRule
            .waitForNavigationContext {
                it.instruction.navigationKey is ComposeManagedResultFlow
            }
    }

    fun assertFirstResultActive(): FirstResultRobot {
        composeRule.onNodeWithText("First Result")
            .assertExists()
        return FirstResultRobot(composeRule)
    }

    fun assertPresentedResultActive(): PresentedResultRobot {
        composeRule.onNodeWithText("Presented")
            .assertExists()
        return PresentedResultRobot(composeRule)
    }

    fun assertSecondResultActive(): SecondResultRobot {
        composeRule.onNodeWithText("Second Result")
            .assertExists()
        composeRule.onNodeWithText("Has extra: ${ComposeManagedResultFlow.hashCode()}")
            .assertExists()
        return SecondResultRobot(composeRule)
    }

    fun assertTransientResultActive(): TransientResultRobot {
        composeRule.onNodeWithText("Transient Result")
            .assertExists()
        return TransientResultRobot(composeRule)
    }

    fun assertThirdResultActive(): ThirdResultRobot {
        composeRule.onNodeWithText("Third Result")
            .assertExists()
        return ThirdResultRobot(composeRule)
    }

    fun assertFinalScreenActive(): FinalScreenRobot {
        composeRule.onNodeWithText("Final Screen")
            .assertExists()
        return FinalScreenRobot(composeRule)
    }

    class FirstResultRobot(
        private val composeRule: ComposeTestRule
    ) {
        init {
            composeRule.waitForNavigationContext {
                it.instruction.navigationKey is ComposeManagedResultFlow.FirstResult
            }
        }

        fun continueA(): ComposeManagedResultFlowRobot {
            composeRule.onNodeWithText("First Result")
                .onSiblings()
                .filterToOne(hasText("Continue (A)"))
                .performClick()
            return ComposeManagedResultFlowRobot(composeRule)
        }

        fun continueB(): ComposeManagedResultFlowRobot {
            composeRule.onNodeWithText("First Result")
                .onSiblings()
                .filterToOne(hasText("Continue (B)"))
                .performClick()
            return ComposeManagedResultFlowRobot(composeRule)
        }
    }

    class PresentedResultRobot(
        private val composeRule: ComposeTestRule
    ) {
        init {
            composeRule.waitForNavigationContext {
                it.instruction.navigationKey is ComposeManagedResultFlow.PresentedResult
            }
        }

        fun continueA(): ComposeManagedResultFlowRobot {
            composeRule.onNodeWithText("Presented")
                .onSiblings()
                .filterToOne(hasText("Continue (A)"))
                .performClick()
            return ComposeManagedResultFlowRobot(composeRule)
        }

        fun continueB(): ComposeManagedResultFlowRobot {
            composeRule.onNodeWithText("Presented")
                .onSiblings()
                .filterToOne(hasText("Continue (B)"))
                .performClick()
            return ComposeManagedResultFlowRobot(composeRule)
        }
    }

    class SecondResultRobot(
        private val composeRule: ComposeTestRule
    ) {
        init {
            composeRule.waitForNavigationContext {
                it.instruction.navigationKey is ComposeManagedResultFlow.SecondResult
            }
        }

        fun continueA(): ComposeManagedResultFlowRobot {
            composeRule.onNodeWithText("Second Result")
                .onSiblings()
                .filterToOne(hasText("Continue (A)"))
                .performClick()
            return ComposeManagedResultFlowRobot(composeRule)
        }

        fun continueB(): ComposeManagedResultFlowRobot {
            composeRule.onNodeWithText("Second Result")
                .onSiblings()
                .filterToOne(hasText("Continue (B)"))
                .performClick()
            return ComposeManagedResultFlowRobot(composeRule)
        }
    }

    class TransientResultRobot(
        val composeRule: ComposeTestRule,
    ) {
        init {
            composeRule.waitForNavigationContext {
                it.instruction.navigationKey is ComposeManagedResultFlow.TransientResult
            }
        }

        fun continueA(): ComposeManagedResultFlowRobot {
            composeRule.onNodeWithText("Transient Result")
                .onSiblings()
                .filterToOne(hasText("Continue (A)"))
                .performClick()
            return ComposeManagedResultFlowRobot(composeRule)
        }

        fun continueB(): ComposeManagedResultFlowRobot {
            composeRule.onNodeWithText("Transient Result")
                .onSiblings()
                .filterToOne(hasText("Continue (B)"))
                .performClick()
            return ComposeManagedResultFlowRobot(composeRule)
        }
    }

    class ThirdResultRobot(
        val composeRule: ComposeTestRule,
    ) {
        init {
            composeRule.waitForNavigationContext {
                it.instruction.navigationKey is ComposeManagedResultFlow.ThirdResult
            }
        }

        fun continueA(): ComposeManagedResultFlowRobot {
            composeRule.onNodeWithText("Third Result")
                .onSiblings()
                .filterToOne(hasText("Continue (A)"))
                .performClick()
            return ComposeManagedResultFlowRobot(composeRule)
        }

        fun continueB(): ComposeManagedResultFlowRobot {
            composeRule.onNodeWithText("Third Result")
                .onSiblings()
                .filterToOne(hasText("Continue (B)"))
                .performClick()
            return ComposeManagedResultFlowRobot(composeRule)
        }
    }

    class FinalScreenRobot(
        val composeRule: ComposeTestRule,
    ) {

        init {
            composeRule.waitForNavigationContext {
                it.instruction.navigationKey is ComposeManagedResultFlow.FinalScreen
            }
        }

        fun editFirst(): ComposeManagedResultFlowRobot {
            composeRule.onNodeWithText("Final Screen")
                .onSiblings()
                .filterToOne(hasText("Edit First Result"))
                .performClick()
            return ComposeManagedResultFlowRobot(composeRule)
        }

        fun editSecond(): ComposeManagedResultFlowRobot {
            composeRule.onNodeWithText("Final Screen")
                .onSiblings()
                .filterToOne(hasText("Edit Second Result"))
                .performClick()
            return ComposeManagedResultFlowRobot(composeRule)
        }

        fun editThird(): ComposeManagedResultFlowRobot {
            composeRule.onNodeWithText("Final Screen")
                .onSiblings()
                .filterToOne(hasText("Edit Third Result"))
                .performClick()
            return ComposeManagedResultFlowRobot(composeRule)
        }

        fun assertResultState(
            first: String? = null,
            presented: String? = null,
            second: String? = null,
            transient: String? = null,
            third: String? = null,
        ): FinalScreenRobot {
            if (first != null) {
                composeRule.onNodeWithText("First Result: $first").assertExists()
            }
            if (presented != null) {
                composeRule.onNodeWithText("Presented Result: $presented").assertExists()
            }
            if (second != null) {
                composeRule.onNodeWithText("Second Result: $second").assertExists()
            }
            if (transient != null) {
                composeRule.onNodeWithText("Transient Result: $transient").assertExists()
            }
            if (third != null) {
                composeRule.onNodeWithText("Third Result: $third").assertExists()
            }
            return this
        }
    }
}