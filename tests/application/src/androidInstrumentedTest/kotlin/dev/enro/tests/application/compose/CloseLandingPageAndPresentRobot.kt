package dev.enro.tests.application.compose

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.performClick
import dev.enro.context.RootContext
import dev.enro.tests.application.waitForNavigationContext
import dev.enro.tests.application.waitForNavigationHandle

class CloseLandingPageAndPresentRobot(
    private val composeRule: ComposeTestRule
) {
    init {
        composeRule.waitForNavigationContext {
            it is RootContext && it.parent is CloseRootAndPresentActivity
        }
    }

    fun continueToInitialScreen(): InitialScreenRobot {
        // First verify we're on the landing page
        composeRule.onNode(hasText("Landing Page Screen")).assertExists()
        
        // Click the Continue button
        composeRule.onNode(hasText("Continue")).performClick()
        
        return InitialScreenRobot(composeRule)
    }
}

class InitialScreenRobot(
    private val composeRule: ComposeTestRule
) {
    init {
        composeRule.waitForNavigationHandle {
            it.key is InitialDestination
        }
    }

    fun presentBottomSheet(): BottomSheetRobot {
        composeRule.onNode(hasText("Present Bottom Sheet")).performClick()
        
        return BottomSheetRobot(composeRule)
    }
}

class BottomSheetRobot(
    private val composeRule: ComposeTestRule
) {
    init {
        composeRule.waitForNavigationHandle {
            it.key is PresentedBottomSheetDestination
        }
    }

    fun closeBottomSheet(): InitialScreenRobot {
        composeRule.onNode(hasText("Close")).performClick()
        
        return InitialScreenRobot(composeRule)
    }
}