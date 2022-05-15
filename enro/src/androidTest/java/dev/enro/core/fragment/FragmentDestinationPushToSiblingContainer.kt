package dev.enro.core.fragment

import dev.enro.core.compose.ComposableDestination
import dev.enro.core.container.setActive
import dev.enro.core.destinations.*
import dev.enro.core.parentContext
import org.junit.Test

class FragmentDestinationPushToSiblingContainer {

    @Test
    fun givenFragmentDestination_whenExecutingPushToSiblingContainer_andTargetIsComposableDestination_thenCorrectDestinationIsOpened() {
        val root = launchFragmentRoot()
        root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoChildContainer)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToSecondary>(IntoSiblingContainer)
    }

    @Test
    fun givenFragmentDestination_whenExecutingPushToSiblingContainer_andTargetIsComposableDestination_andSiblingPushesAgain_thenCorrectDestinationIsOpened() {
        val root = launchFragmentRoot()
        root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoChildContainer)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToSecondary>(IntoSiblingContainer)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToSecondary>(IntoSameContainer)
    }

    @Test
    fun givenFragmentDestination_whenExecutingPushToSiblingContainer_andTargetIsComposableDestination_andDestinationIsClosed_thenPreviousDestinationIsActive() {
        val root = launchFragmentRoot()
        val firstKey = ComposableDestinations.PushesToPrimary()
        val secondKey = ComposableDestinations.PushesToSecondary()
        root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoChildContainer, firstKey)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToSecondary>(IntoSiblingContainer, secondKey)
            .assertClosesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(firstKey)
    }

    @Test
    fun givenFragmentDestination_whenExecutingMultiplePushesToSiblingContainer_andTargetIsComposableDestination_andDestinationIsClosed_thenPreviousDestinationIsActive() {
        val root = launchFragmentRoot()
        val expectedClose = ComposableDestinations.PushesToSecondary()
        root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoChildContainer)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToSecondary>(IntoSiblingContainer, expectedClose)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToSecondary>(IntoSameContainer)
            .assertClosesTo<ComposableDestination, ComposableDestinations.PushesToSecondary>(expectedClose)
    }

    @Test
    fun givenFragmentDestination_whenExecutingPushToSiblingContainer_andTargetIsComposableDestination_andDestinationDeliversResult_thenResultIsDelivered() {
        val root = launchFragmentRoot()
        val firstKey = ComposableDestinations.PushesToPrimary()
        val secondKey = ComposableDestinations.PushesToSecondary()
        root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoChildContainer, firstKey)
            .assertPushesForResultTo<ComposableDestination, ComposableDestinations.PushesToSecondary>(IntoSiblingContainer, secondKey)
            .assertClosesWithResultTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(firstKey)
    }

    @Test
    fun givenFragmentDestination_whenExecutingMultiplePushesToSiblingContainer_andTargetIsComposableDestination_andDestinationDeliversResult_thenResultIsDelivered() {
        val root = launchFragmentRoot()
        val firstKey = ComposableDestinations.PushesToPrimary()
        val secondKey = ComposableDestinations.PushesToSecondary()

        val primary = root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoChildContainer, firstKey)
        val primaryContainer = root.navigationContext.containerManager.activeContainer

        primary.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToSecondary>(IntoSiblingContainer)
        primary.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToSecondary>(IntoSiblingContainer)
        primary.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToSecondary>(IntoSiblingContainer)

        primaryContainer?.setActive() // TODO Should this be necessary? When a result is delivered, should that container automatically become active?

        primary.assertPushesForResultTo<ComposableDestination, ComposableDestinations.PushesToSecondary>(IntoSiblingContainer, secondKey)
            .assertClosesWithResultTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(firstKey)
    }
}