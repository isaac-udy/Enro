package dev.enro.tests.application.compose

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.performScrollToIndex
import dev.enro.tests.application.waitForNavigationHandle

class LazyColumnRobot(
    private val composeRule: ComposeTestRule
) {
    init {
        composeRule.waitForNavigationHandle {
            it.key is LazyColumn
        }
    }

    fun verifyLazyColumnVisible(): LazyColumnRobot {
        composeRule
            .onNode(hasTestTag(LazyColumn.testTag))
            .assertExists()
        return this
    }

    fun scrollToItem(index: Int): LazyColumnRobot {
        // Find the lazy column and scroll to the specified item
        composeRule
            .onNode(hasTestTag(LazyColumn.testTag))
            .performScrollToIndex(index)
        return this
    }

    fun verifyItemVisible(index: Int): LazyColumnRobot {
        // Verify that the item with the specified index is visible
        composeRule.onNode(hasText("$index")).assertExists()
        return this
    }
}