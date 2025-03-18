package dev.enro.tests.application.compose

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.enro.tests.application.waitForNavigationHandle
import dev.enro.tests.application.compose.ComposeSavePrimitives

class ComposeSavePrimitivesRobot(
    private val composeRule: ComposeTestRule
) {
    private val intTextTestTag = "ComposeSavePrimitivesInnerScreen.intText"

    init {
        composeRule.waitForNavigationHandle {
            it.key is ComposeSavePrimitives
        }
    }


    @OptIn(ExperimentalTestApi::class)
    fun saveState(): ComposeSavePrimitivesRobot {
        composeRule.onNodeWithText("Save State")
            .performClick()
        composeRule.waitUntilDoesNotExist(hasTestTag(intTextTestTag))
        return this
    }

    @OptIn(ExperimentalTestApi::class)
    fun restoreState(): ComposeSavePrimitivesRobot {
        composeRule.onNodeWithText("Restore State")
            .performClick()
        composeRule.waitUntilAtLeastOneExists(hasTestTag(intTextTestTag))
        return this
    }

    @OptIn(ExperimentalTestApi::class)
    fun getIntString(): String {
        composeRule.waitUntilAtLeastOneExists(hasTestTag(intTextTestTag))
        return composeRule.onNodeWithTag(intTextTestTag)
            .fetchSemanticsNode()
            .config[SemanticsProperties.Text]
            .first()
            .text
    }

    @OptIn(ExperimentalTestApi::class)
    fun assertIntString(expected: String): ComposeSavePrimitivesRobot {
        composeRule.waitUntilAtLeastOneExists(hasTestTag(intTextTestTag))
        composeRule.onNodeWithTag(intTextTestTag)
            .assertTextEquals(expected)
        return this
    }

}