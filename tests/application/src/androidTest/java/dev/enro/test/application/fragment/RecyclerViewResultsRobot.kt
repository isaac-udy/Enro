package dev.enro.test.application.fragment

import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onSiblings
import androidx.compose.ui.test.performClick
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import dev.enro.test.application.waitForNavigationHandle
import dev.enro.tests.application.fragment.RecyclerViewItem
import dev.enro.tests.application.fragment.RecyclerViewResults
import org.hamcrest.Matchers
import kotlin.reflect.KClass

class RecyclerViewResultsRobot(
    private val composeRule: ComposeTestRule
) {
    init {
        composeRule.waitForNavigationHandle {
            it.key is RecyclerViewResults.RootFragment
        }
    }

    fun launchResultFor(position: Int): ResultFragmentRobot {
        onView(isAssignableFrom(RecyclerView::class.java))
            .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(position))
            .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(position, click()))

        return ResultFragmentRobot(composeRule)
    }

    fun assertItemAtPositionIs(position: Int, itemType: KClass<out RecyclerViewItem>) : RecyclerViewResultsRobot {
        onView(isAssignableFrom(RecyclerView::class.java))
            .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(position))

        val expectedText = when (itemType) {
            RecyclerViewItem.Compose::class -> "Compose Item $position"
            RecyclerViewItem.ComposeWithRememberSaveableResult::class -> "Compose Item (rememberSaveable) $position"
            RecyclerViewItem.ComposeWithExternalResultChannel::class -> "Compose Item (external result channel) $position"
            RecyclerViewItem.ViewWithInternalResultChannel::class -> "View Item (Internal) $position"
            RecyclerViewItem.ViewWithExternalResultChannel::class -> "View Item (External) $position"
            else -> error("Unsupported RecyclerViewItem type")
        }

        val isCompose = itemType == RecyclerViewItem.Compose::class ||
                itemType == RecyclerViewItem.ComposeWithRememberSaveableResult::class ||
                itemType == RecyclerViewItem.ComposeWithExternalResultChannel::class

        if (isCompose) {
            composeRule.onAllNodesWithText(expectedText)
                .fetchSemanticsNodes()
                .isNotEmpty()
        } else {
            onView(withText(expectedText))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        }
        return this
    }

    fun assertResultAtPositionIs(position: Int, expectedResult: String): RecyclerViewResultsRobot {
        onView(isAssignableFrom(RecyclerView::class.java))
            .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(position))

        val titleText = when (position % 5) {
            0 -> "Compose Item $position"
            1 -> "Compose Item (rememberSaveable) $position"
            2 -> "Compose Item (external result channel) $position"
            3 -> "View Item (Internal) $position"
            4 -> "View Item (External) $position"
            else -> error("Unsupported RecyclerViewItem type")
        }
        val isCompose = position % 5 == 0 || position % 5 == 1 || position % 5 == 2

        if (isCompose) {
            composeRule
                .onNodeWithText(titleText)
                .onSiblings()
                .filterToOne(hasText("Result: $expectedResult"))
                .isDisplayed()
        } else {
            onView(Matchers.allOf(ViewMatchers.hasSibling(withText(titleText)), withText(expectedResult)))
                .check(ViewAssertions.matches(isDisplayed()))
        }
        return this
    }
}

class ResultFragmentRobot(
    private val composeRule: ComposeTestRule
) {
    init {
        composeRule.onAllNodes(isRoot())
            .assertAll(isRoot())

        composeRule.waitForNavigationHandle {
            it.key is RecyclerViewResults.ResultFragment
        }
    }

    fun selectResultA(): RecyclerViewResultsRobot {
        composeRule.onNodeWithText("Result: A")
            .performClick()
        return RecyclerViewResultsRobot(composeRule)
    }

    fun selectResultB(): RecyclerViewResultsRobot {
        composeRule.onNodeWithText("Result: B")
            .performClick()
        return RecyclerViewResultsRobot(composeRule)
    }

    fun selectResultC(): RecyclerViewResultsRobot {
        composeRule.onNodeWithText("Result: C")
            .performClick()
        return RecyclerViewResultsRobot(composeRule)
    }
}