package dev.enro.core.compose

import androidx.fragment.app.Fragment
import dev.enro.core.destinations.*
import dev.enro.core.parentContainer
import org.junit.Assert.assertEquals
import org.junit.Test

class ComposableDestinationPush {
    @Test
    fun givenComposableDestination_whenExecutingPush_andTargetIsComposableDestination_thenCorrectDestinationIsOpened() {
        val root = launchComposableRoot()
        root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(
            IntoChildContainer
        )
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(
                IntoSameContainer
            )
    }

    @Test
    fun givenComposableDestination_whenExecutingPush_andTargetIsComposableDestination_andDestinationIsClosed_thenPreviousDestinationIsActive() {
        val root = launchComposableRoot()
        val firstKey = ComposableDestinations.PushesToPrimary()
        val secondKey = ComposableDestinations.PushesToPrimary()
        root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(
            IntoChildContainer, firstKey)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(
                IntoSameContainer, secondKey)
            .assertClosesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(firstKey)
    }

    @Test
    fun givenComposableDestination_whenExecutingPush_andTargetIsComposableDestination_andDestinationDeliversResult_thenResultIsDelivered() {
        val root = launchComposableRoot()
        val firstKey = ComposableDestinations.PushesToPrimary()
        val secondKey = ComposableDestinations.PushesToPrimary()
        root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoChildContainer, firstKey)
            .assertPushesForResultTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoSameContainer, secondKey)
            .assertClosesWithResultTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(firstKey)
    }

    @Test
    fun givenComposableRootDestination_whenPushingComposables_thenComposablesArePushedIntoComposableContainerNotAsFragments() {
        val root = launchComposableRoot()
        val composableContainer = root.navigationContext.parentContainer()
        /**
         * When a composables is launched into the root of an activity, the first composable container should accept all navigation keys,
         * and allow additional composable pushes within that container, rather than wrapping each composable in a Fragment Host.
         *
         * This checks that the composable destinations are opened, but also explicitly ensures that they are all opened into
         * exactly the same composable container, rather than just being opened "IntoSameContainer", which allows for destinations
         * to be opened into the same container while hosted in some other context type.
         */
        root.assertPushesTo<ComposableDestination, ComposableDestinations.Pushable>(IntoSameContainer)
            .also { assertEquals(composableContainer, it.navigationContext.parentContainer()) }
            .assertPushesTo<ComposableDestination, ComposableDestinations.Pushable>(IntoSameContainer)
            .also { assertEquals(composableContainer, it.navigationContext.parentContainer()) }
            .assertPushesTo<ComposableDestination, ComposableDestinations.Pushable>(IntoSameContainer)
            .also { assertEquals(composableContainer, it.navigationContext.parentContainer()) }
    }

    @Test
    fun givenComposableRootDestination_whenPushingComposablesAndFragments_thenClosingFragmentsMaintainsComposableState() {
        val root = launchComposableRoot()

        val firstComposable = ComposableDestinations.Pushable()
        val secondComposable = ComposableDestinations.Pushable()
        val thirdComposable = ComposableDestinations.Pushable()
        val firstFragment = FragmentDestinations.Pushable()
        val secondFragment = FragmentDestinations.Pushable()

        root.assertPushesTo<ComposableDestination, ComposableDestinations.Pushable>(IntoSameContainer, firstComposable)
            .assertPushesTo<ComposableDestination, ComposableDestinations.Pushable>(IntoSameContainer, secondComposable)
            .assertPushesTo<ComposableDestination, ComposableDestinations.Pushable>(IntoSameContainer, thirdComposable)
            .assertPushesTo<Fragment, FragmentDestinations.Pushable>(IntoSameContainer, firstFragment)
            .assertPushesTo<Fragment, FragmentDestinations.Pushable>(IntoSameContainer, secondFragment)

            .assertClosesTo<Fragment, FragmentDestinations.Pushable>(firstFragment)
            .assertClosesTo<ComposableDestination, ComposableDestinations.Pushable>(thirdComposable)
            .assertClosesTo<ComposableDestination, ComposableDestinations.Pushable>(secondComposable)
            .assertClosesTo<ComposableDestination, ComposableDestinations.Pushable>(firstComposable)
    }
}