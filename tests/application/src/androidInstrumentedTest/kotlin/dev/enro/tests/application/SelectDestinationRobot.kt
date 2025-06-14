package dev.enro.tests.application

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onSiblings
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
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
import dev.enro.tests.application.compose.results.ResultsWithMetadataRobot
import dev.enro.tests.application.fragment.FragmentPresentationRobot
import dev.enro.tests.application.fragment.UnboundBottomSheetRobot
import dev.enro.tests.application.managedflow.ManagedFlowInComposableRobot

class SelectDestinationRobot(
    private val composeRule: ComposeTestRule
) {

    init {
        composeRule.waitForNavigationHandle {
            it.key is SelectDestination
        }
    }

    fun openBottomSheetCloseAndPresent(): BottomSheetCloseAndPresentRobot {
        scrollTo("Bottom Sheet Close And Present")
            .onSiblings()
            .filterToOne(hasText("Open"))
            .performClick()

        return BottomSheetCloseAndPresentRobot(composeRule)
    }

    fun openBottomSheetChangeSize(): BottomSheetChangeSizeRobot {
        scrollTo("Bottom Sheet Change Size")
            .onSiblings()
            .filterToOne(hasText("Open"))
            .performClick()

        return BottomSheetChangeSizeRobot(composeRule)
    }

    fun openSimpleActivity(): SimpleActivityRobot {
        scrollTo("Simple Activity")
            .onSiblings()
            .filterToOne(hasText("Open"))
            .performClick()

        return SimpleActivityRobot(composeRule)
    }

    fun openUnboundBottomSheet(): UnboundBottomSheetRobot {
        composeRule
            .onNode(hasText("Unbound Bottom Sheet"))
            .performScrollTo()
            .onSiblings()
            .filterToOne(hasText("Open"))
            .performClick()

        return UnboundBottomSheetRobot(composeRule)
    }

    fun openBottomNavigation(): BottomNavigationRobot {
        composeRule.waitForIdle()
        scrollTo("Bottom Navigation")
            .onSiblings()
            .filterToOne(hasText("Open"))
            .performClick()

        return BottomNavigationRobot(composeRule)
    }

    fun openSyntheticViewModelAccess(): SyntheticViewModelAccessRobot {
        scrollTo("Synthetic View Model Access")
            .onSiblings()
            .filterToOne(hasText("Open"))
            .performClick()

        return SyntheticViewModelAccessRobot(composeRule)
    }

    fun openFindContext(): FindContextRobot {
        scrollTo("Find Context")
            .onSiblings()
            .filterToOne(hasText("Open"))
            .performClick()

        return FindContextRobot(composeRule)
    }

    fun openComposeEmbeddedResultFlow(): ComposeEmbeddedResultFlowRobot {
        scrollTo("Compose Embedded Result Flow")
            .onSiblings()
            .filterToOne(hasText("Open"))
            .performClick()

        return ComposeEmbeddedResultFlowRobot(composeRule)
    }

    fun openComposeManagedResultFlow(): ComposeManagedResultFlowRobot {
        scrollTo("Compose Managed Result Flow")
            .onSiblings()
            .filterToOne(hasText("Open"))
            .performClick()

        return ComposeManagedResultFlowRobot(composeRule)
    }

    fun openComposeAsyncManagedResultFlow(): ComposeAsyncManagedResultFlowRobot {
        scrollTo("Compose Async Managed Result Flow")
            .onSiblings()
            .filterToOne(hasText("Open"))
            .performClick()

        return ComposeAsyncManagedResultFlowRobot(composeRule)
    }

    fun openComposeNestedResults(): ComposeNestedResultsRobot {
        scrollTo("Compose Nested Results")
            .onSiblings()
            .filterToOne(hasText("Open"))
            .performClick()

        return ComposeNestedResultsRobot(composeRule)
    }

    fun openResultsWithMetadata(): ResultsWithMetadataRobot {
        scrollTo("Results With Metadata")
            .onSiblings()
            .filterToOne(hasText("Open"))
            .performClick()

        return ResultsWithMetadataRobot(composeRule)
    }

    fun openComposeManagedResultsWithNestedFlowAndEmptyRoot(): ComposeManagedResultsWithNestedFlowAndEmptyRootRobot {
        scrollTo("Compose Managed Results With Nested Flow And Empty Root")
            .onSiblings()
            .filterToOne(hasText("Open"))
            .performClick()

        return ComposeManagedResultsWithNestedFlowAndEmptyRootRobot(composeRule)
    }

    fun openComposeMixedResultTypes(): ComposeMixedResultTypesRobot {
        scrollTo("Compose Mixed Result Types")
            .onSiblings()
            .filterToOne(hasText("Open"))
            .performClick()
        return ComposeMixedResultTypesRobot(composeRule)
    }

    fun openManagedFlowInComposable(): ManagedFlowInComposableRobot {
        scrollTo("Managed Flow In Composable")
            .onSiblings()
            .filterToOne(hasText("Open"))
            .performClick()
        return ManagedFlowInComposableRobot(composeRule)
    }

    fun openComposeSavePrimitives(): ComposeSavePrimitivesRobot {
        scrollTo("Compose Save Primitives")
            .onSiblings()
            .filterToOne(hasText("Open"))
            .performClick()
        return ComposeSavePrimitivesRobot(composeRule)
    }

    fun openCloseLandingPageAndPresent(): CloseLandingPageAndPresentRobot {
        scrollTo("Close Landing Page And Present")
            .onSiblings()
            .filterToOne(hasText("Open"))
            .performClick()
        return CloseLandingPageAndPresentRobot(composeRule)
    }

    fun openHorizontalPager(): HorizontalPagerRobot {
        scrollTo("Horizontal Pager")
            .onSiblings()
            .filterToOne(hasText("Open"))
            .performClick()
        return HorizontalPagerRobot(composeRule)
    }

    fun openLazyColumn(): LazyColumnRobot {
        scrollTo("Lazy Column")
            .onSiblings()
            .filterToOne(hasText("Open"))
            .performClick()
        return LazyColumnRobot(composeRule)
    }

    fun openComposeAnimations(): ComposeAnimationsRobot {
        scrollTo("Compose Animations")
            .onSiblings()
            .filterToOne(hasText("Open"))
            .performClick()
        return ComposeAnimationsRobot(composeRule)
    }
    
    fun openFragmentPresentation(): FragmentPresentationRobot {
        scrollTo("Fragment Presentation")
            .onSiblings()
            .filterToOne(hasText("Open"))
            .performClick()
        return FragmentPresentationRobot(composeRule)
    }

    fun scrollTo(text: String): SemanticsNodeInteraction {
        composeRule.onNodeWithTag("SelectDestinationLazyColumn")
            .performScrollToNode(hasText(text))
        return composeRule.onNode(hasText(text))
    }

}