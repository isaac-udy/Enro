package dev.enro.tests.application

import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onSiblings
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import dev.enro.tests.application.activity.SimpleActivityRobot
import dev.enro.tests.application.compose.BottomNavigationRobot
import dev.enro.tests.application.compose.BottomSheetChangeSizeRobot
import dev.enro.tests.application.compose.BottomSheetCloseAndPresentRobot
import dev.enro.tests.application.compose.CloseLandingPageAndPresentRobot
import dev.enro.tests.application.compose.ComposeAnimationsRobot
import dev.enro.tests.application.compose.ComposeSavePrimitivesRobot
import dev.enro.tests.application.compose.FindContextRobot
import dev.enro.tests.application.compose.HorizontalPagerRobot
import dev.enro.tests.application.compose.LazyColumnRobot
import dev.enro.tests.application.compose.SyntheticViewModelAccessRobot
import dev.enro.tests.application.compose.results.ComposeAsyncManagedResultFlowRobot
import dev.enro.tests.application.compose.results.ComposeEmbeddedResultFlowRobot
import dev.enro.tests.application.compose.results.ComposeManagedResultFlowRobot
import dev.enro.tests.application.compose.results.ComposeManagedResultsWithNestedFlowAndEmptyRootRobot
import dev.enro.tests.application.compose.results.ComposeMixedResultTypesRobot
import dev.enro.tests.application.compose.results.ComposeNestedResultsRobot
import dev.enro.tests.application.compose.results.ResultsWithExtraRobot
import dev.enro.tests.application.fragment.UnboundBottomSheetRobot
import dev.enro.tests.application.managedflow.ManagedFlowInComposableRobot
import dev.enro.tests.application.managedflow.ManagedFlowInFragmentRobot

class SelectDestinationRobot(
    private val composeRule: ComposeTestRule
) {

    init {
        composeRule.waitForNavigationHandle {
            it.key is SelectDestination
        }
    }

    fun openBottomSheetCloseAndPresent(): BottomSheetCloseAndPresentRobot {
        composeRule.onNode(hasText("Bottom Sheet Close And Present"))
            .performScrollTo()
            .onSiblings()
            .filterToOne(hasText("Push"))
            .performClick()

        return BottomSheetCloseAndPresentRobot(composeRule)
    }

    fun openBottomSheetChangeSize(): BottomSheetChangeSizeRobot {
        composeRule.onNode(hasText("Bottom Sheet Change Size"))
            .performScrollTo()
            .onSiblings()
            .filterToOne(hasText("Push"))
            .performClick()

        return BottomSheetChangeSizeRobot(composeRule)
    }

    fun openSimpleActivity(): SimpleActivityRobot {
        composeRule.onNode(hasText("Simple Activity"))
            .performScrollTo()
            .onSiblings()
            .filterToOne(hasText("Present"))
            .performClick()

        return SimpleActivityRobot(composeRule)
    }

    fun openUnboundBottomSheet(): UnboundBottomSheetRobot {
        composeRule
            .onNode(hasText("Unbound Bottom Sheet"))
            .performScrollTo()
            .onSiblings()
            .filterToOne(hasText("Present"))
            .performClick()

        return UnboundBottomSheetRobot(composeRule)
    }

    fun openBottomNavigation(): BottomNavigationRobot {
        composeRule.onNode(hasText("Bottom Navigation"))
            .performScrollTo()
            .onSiblings()
            .filterToOne(hasText("Push"))
            .performClick()

        return BottomNavigationRobot(composeRule)
    }

    fun openSyntheticViewModelAccess(): SyntheticViewModelAccessRobot {
        composeRule.onNode(hasText("Synthetic View Model Access"))
            .performScrollTo()
            .onSiblings()
            .filterToOne(hasText("Push"))
            .performClick()

        return SyntheticViewModelAccessRobot(composeRule)
    }

    fun openFindContext(): FindContextRobot {
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

    fun openComposeManagedResultFlow(): ComposeManagedResultFlowRobot {
        composeRule.onNode(hasText("Compose Managed Result Flow"))
            .performScrollTo()
            .onSiblings()
            .filterToOne(hasText("Push"))
            .performClick()

        return ComposeManagedResultFlowRobot(composeRule)
    }

    fun openComposeAsyncManagedResultFlow(): ComposeAsyncManagedResultFlowRobot {
        composeRule.onNode(hasText("Compose Async Managed Result Flow"))
            .performScrollTo()
            .onSiblings()
            .filterToOne(hasText("Push"))
            .performClick()

        return ComposeAsyncManagedResultFlowRobot(composeRule)
    }

    fun openComposeNestedResults(): ComposeNestedResultsRobot {
        composeRule.onNode(hasText("Compose Nested Results"))
            .performScrollTo()
            .onSiblings()
            .filterToOne(hasText("Push"))
            .performClick()

        return ComposeNestedResultsRobot(composeRule)
    }

    fun openResultsWithExtra(): ResultsWithExtraRobot {
        composeRule.onNode(hasText("Results With Extra"))
            .performScrollTo()
            .onSiblings()
            .filterToOne(hasText("Push"))
            .performClick()

        return ResultsWithExtraRobot(composeRule)
    }

    fun openComposeManagedResultsWithNestedFlowAndEmptyRoot(): ComposeManagedResultsWithNestedFlowAndEmptyRootRobot {
        composeRule.onNode(hasText("Compose Managed Results With Nested Flow And Empty Root"))
            .performScrollTo()
            .onSiblings()
            .filterToOne(hasText("Push"))
            .performClick()

        return ComposeManagedResultsWithNestedFlowAndEmptyRootRobot(composeRule)
    }

    fun openComposeMixedResultTypes(): ComposeMixedResultTypesRobot {
        composeRule.onNode(hasText("Compose Mixed Result Types"))
            .performScrollTo()
            .onSiblings()
            .filterToOne(hasText("Push"))
            .performClick()
        return ComposeMixedResultTypesRobot(composeRule)
    }

    fun openManagedFlowInComposable(): ManagedFlowInComposableRobot {
        composeRule.onNode(hasText("Managed Flow In Composable"))
            .performScrollTo()
            .onSiblings()
            .filterToOne(hasText("Push"))
            .performClick()
        return ManagedFlowInComposableRobot(composeRule)
    }

    fun openManagedFlowInFragment(): ManagedFlowInFragmentRobot {
        composeRule.onNode(hasText("Managed Flow In Fragment"))
            .performScrollTo()
            .onSiblings()
            .filterToOne(hasText("Present"))
            .performClick()
        return ManagedFlowInFragmentRobot(composeRule)
    }

    fun openComposeSavePrimitives(): ComposeSavePrimitivesRobot {
        composeRule.onNode(hasText("Compose Save Primitives"))
            .performScrollTo()
            .onSiblings()
            .filterToOne(hasText("Push"))
            .performClick()
        return ComposeSavePrimitivesRobot(composeRule)
    }
    
    fun openCloseLandingPageAndPresent(): CloseLandingPageAndPresentRobot {
        composeRule.onNode(hasText("Close Landing Page And Present"))
            .performScrollTo()
            .onSiblings()
            .filterToOne(hasText("Present"))
            .performClick()
        return CloseLandingPageAndPresentRobot(composeRule)
    }
    
    fun openHorizontalPager(): HorizontalPagerRobot {
        composeRule.onNode(hasText("Horizontal Pager"))
            .performScrollTo()
            .onSiblings()
            .filterToOne(hasText("Push"))
            .performClick()
        return HorizontalPagerRobot(composeRule)
    }
    
    fun openLazyColumn(): LazyColumnRobot {
        composeRule.onNode(hasText("Lazy Column"))
            .performScrollTo()
            .onSiblings()
            .filterToOne(hasText("Push"))
            .performClick()
        return LazyColumnRobot(composeRule)
    }
    
    fun openComposeAnimations(): ComposeAnimationsRobot {
        composeRule.onNode(hasText("Compose Animations"))
            .performScrollTo()
            .onSiblings()
            .filterToOne(hasText("Push"))
            .performClick()
        return ComposeAnimationsRobot(composeRule)
    }
}