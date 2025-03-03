package dev.enro.test.application.compose.results

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.enro.test.application.waitForNavigationHandle
import dev.enro.tests.application.compose.results.ComposeMixedResultTypes

@OptIn(ExperimentalTestApi::class)
class ComposeMixedResultTypesRobot(
    private val composeRule: ComposeTestRule
) {
    init {
        composeRule.waitForNavigationHandle {
            it.key is ComposeMixedResultTypes
        }
    }

    fun assertStringResult(): ComposeMixedResultTypesRobot {
        composeRule.onNodeWithText("Get Result: String")
            .assertExists()
            .performClick()

        composeRule.waitUntilAtLeastOneExists(hasText("Send Result"))
        composeRule.onNodeWithText("Send Result")
            .assertExists()
            .performClick()

        composeRule.waitUntilAtLeastOneExists(hasText("Mixed Result Types"))
        composeRule.onNodeWithText(
            "Current Result: java.lang.String \"This is a String\""
        )
        return this
    }

    fun assertIntResult(): ComposeMixedResultTypesRobot {
        composeRule.onNodeWithText("Get Result: Int")
            .assertExists()
            .performClick()

        composeRule.waitUntilAtLeastOneExists(hasText("Send Result"))
        composeRule.onNodeWithText("Send Result")
            .assertExists()
            .performClick()

        composeRule.waitUntilAtLeastOneExists(hasText("Mixed Result Types"))
        composeRule.onNodeWithText(
            "Current Result: int 1"
        )
        return this
    }

    fun assertListStringResult(): ComposeMixedResultTypesRobot {
        composeRule.onNodeWithText("Get Result: List<String>")
            .assertExists()
            .performClick()

        composeRule.waitUntilAtLeastOneExists(hasText("Send Result"))
        composeRule.onNodeWithText("Send Result")
            .assertExists()
            .performClick()

        composeRule.waitUntilAtLeastOneExists(hasText("Mixed Result Types"))
        composeRule.onNodeWithText(
            "Current Result: java.util.Arrays\$ArrayList [wow, nice]"
        )
        return this
    }

    fun assertBooleanResult(): ComposeMixedResultTypesRobot {
        composeRule.onNodeWithText("Get Result: Boolean")
            .assertExists()
            .performClick()

        composeRule.waitUntilAtLeastOneExists(hasText("Send Result"))
        composeRule.onNodeWithText("Send Result")
            .assertExists()
            .performClick()

        composeRule.waitUntilAtLeastOneExists(hasText("Mixed Result Types"))
        composeRule.onNodeWithText(
            "Current Result: boolean true"
        )
        return this
    }

    fun assertObjectResult(): ComposeMixedResultTypesRobot {
        composeRule.onNodeWithText("Get Result: AnotherObject")
            .assertExists()
            .performClick()

        composeRule.waitUntilAtLeastOneExists(hasText("Send Result"))
        composeRule.onNodeWithText("Send Result")
            .assertExists()
            .performClick()

        composeRule.waitUntilAtLeastOneExists(hasText("Mixed Result Types"))
        composeRule.onNodeWithText(
            substring = true,
            text = "Current Result: dev.enro.test.application.compose.results.AnotherObject",
        )
        return this
    }
}