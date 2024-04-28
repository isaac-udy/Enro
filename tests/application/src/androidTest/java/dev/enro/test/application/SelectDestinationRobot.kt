package dev.enro.test.application

import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onSiblings
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import dev.enro.test.application.activity.SimpleActivityRobot
import dev.enro.test.application.compose.BottomNavigationRobot
import dev.enro.test.application.compose.BottomSheetChangeSizeRobot
import dev.enro.test.application.compose.BottomSheetCloseAndPresentRobot
import dev.enro.test.application.compose.FindContextRobot
import dev.enro.test.application.compose.LegacyBottomSheetsRobot
import dev.enro.test.application.compose.SyntheticViewModelAccessRobot
import dev.enro.test.application.compose.results.ComposeEmbeddedResultFlowRobot
import dev.enro.test.application.fragment.UnboundBottomSheetRobot
import dev.enro.tests.application.SelectDestination

class SelectDestinationRobot(
    private val composeRule: ComposeTestRule
) {

    init {
        composeRule.waitForNavigationHandle {
            it.key is SelectDestination
        }
    }

    fun openBottomSheetCloseAndPresent() : BottomSheetCloseAndPresentRobot {
        composeRule.onNode(hasText("Bottom Sheet Close And Present"))
            .performScrollTo()
            .onSiblings()
            .filterToOne(hasText("Push"))
            .performClick()

        return BottomSheetCloseAndPresentRobot(composeRule)
    }

    fun openBottomSheetChangeSize() : BottomSheetChangeSizeRobot {
        composeRule.onNode(hasText("Bottom Sheet Change Size"))
            .performScrollTo()
            .onSiblings()
            .filterToOne(hasText("Push"))
            .performClick()

        return BottomSheetChangeSizeRobot(composeRule)
    }

    fun openLegacyBottomSheets() : LegacyBottomSheetsRobot {
        composeRule.onNode(hasText("Legacy Bottom Sheets"))
            .performScrollTo()
            .onSiblings()
            .filterToOne(hasText("Push"))
            .performClick()

        return LegacyBottomSheetsRobot(composeRule)
    }

    fun openSimpleActivity() : SimpleActivityRobot {
        composeRule.onNode(hasText("Simple Activity"))
            .performScrollTo()
            .onSiblings()
            .filterToOne(hasText("Present"))
            .performClick()

        return SimpleActivityRobot(composeRule)
    }

    fun openUnboundBottomSheet() : UnboundBottomSheetRobot {
        composeRule
            .onNode(hasText("Unbound Bottom Sheet"))
            .performScrollTo()
            .onSiblings()
            .filterToOne(hasText("Present"))
            .performClick()

        return UnboundBottomSheetRobot(composeRule)
    }

    fun openBottomNavigation() : BottomNavigationRobot {
        composeRule.onNode(hasText("Bottom Navigation"))
            .performScrollTo()
            .onSiblings()
            .filterToOne(hasText("Push"))
            .performClick()

        return BottomNavigationRobot(composeRule)
    }

    fun openSyntheticViewModelAccess() : SyntheticViewModelAccessRobot {
        composeRule.onNode(hasText("Synthetic View Model Access"))
            .performScrollTo()
            .onSiblings()
            .filterToOne(hasText("Push"))
            .performClick()

        return SyntheticViewModelAccessRobot(composeRule)
    }

    fun openFindContext() : FindContextRobot {
        composeRule.onNode(hasText("Find Context"))
            .performScrollTo()
            .onSiblings()
            .filterToOne(hasText("Push"))
            .performClick()

        return FindContextRobot(composeRule)
    }

    fun openComposeEmbeddedResultFlow(): ComposeEmbeddedResultFlowRobot {
        composeRule.onNode(hasText("Compose Embedded Result Flow"))
            .performScrollTo()
            .onSiblings()
            .filterToOne(hasText("Push"))
            .performClick()

        return ComposeEmbeddedResultFlowRobot(composeRule)
    }
}