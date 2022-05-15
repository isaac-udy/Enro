package dev.enro.core.fragment

import dev.enro.core.compose.ComposableDestination
import dev.enro.core.destinations.*
import org.junit.Test

class FragmentDestinationPushToChildContainer {
    @Test
    fun givenFragmentDestination_whenExecutingPushToChildContainer_andTargetIsComposableDestination_thenCorrectDestinationIsOpened() {
         val root = launchFragmentRoot()
        root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoChildContainer)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToChildAsPrimary>(IntoChildContainer)
    }

    @Test
    fun givenFragmentDestination_whenExecutingMultiplePushesToChildContainer_andTargetIsComposableDestination_thenCorrectDestinationIsOpened() {
        val root = launchFragmentRoot()
        root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoChildContainer)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToChildAsPrimary>(IntoChildContainer)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToChildAsPrimary>(IntoSameContainer)
    }

    @Test
    fun givenFragmentDestination_whenExecutingPushToChildContainer_andTargetIsComposableDestination_andDestinationIsClosed_thenPreviousDestinationIsActive() {
        val root = launchFragmentRoot()
        val firstKey = ComposableDestinations.PushesToPrimary()
        val secondKey = ComposableDestinations.PushesToChildAsPrimary()
        root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoChildContainer, firstKey)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToChildAsPrimary>(IntoChildContainer, secondKey)
            .assertClosesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(firstKey)
    }

    @Test
    fun givenFragmentDestination_whenExecutingMultiplePushesToChildContainer_andTargetIsComposableDestination_andDestinationIsClosed_thenPreviousDestinationIsActive() {
        val root = launchFragmentRoot()
        val firstKey = ComposableDestinations.PushesToPrimary()
        val secondKey = ComposableDestinations.PushesToChildAsPrimary()
        val thirdKey = ComposableDestinations.PushesToChildAsPrimary()
        root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoChildContainer, firstKey)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToChildAsPrimary>(IntoChildContainer, secondKey)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToChildAsPrimary>(IntoSameContainer, thirdKey)
            .assertClosesTo<ComposableDestination, ComposableDestinations.PushesToChildAsPrimary>(secondKey)
    }

    @Test
    fun givenFragmentDestination_whenExecutingPushToChildContainer_andTargetIsComposableDestination_andDestinationDeliversResult_thenResultIsDelivered() {
         val root = launchFragmentRoot()
        val firstKey = ComposableDestinations.PushesToPrimary()
        val secondKey = ComposableDestinations.PushesToChildAsPrimary()
        root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoChildContainer, firstKey)
            .assertPushesForResultTo<ComposableDestination, ComposableDestinations.PushesToChildAsPrimary>(IntoChildContainer, secondKey)
            .assertClosesWithResultTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(firstKey)
    }

    @Test
    fun givenFragmentDestination_whenExecutingMultiplePushesToChildContainer_andTargetIsComposableDestination_andDestinationDeliversResult_thenResultIsDelivered() {
        val root = launchFragmentRoot()
        val firstKey = ComposableDestinations.PushesToPrimary()
        val secondKey = ComposableDestinations.PushesToChildAsPrimary()
        val thirdKey = ComposableDestinations.PushesToChildAsPrimary()
        root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoChildContainer, firstKey)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToChildAsPrimary>(IntoChildContainer, secondKey)
            .assertPushesForResultTo<ComposableDestination, ComposableDestinations.PushesToChildAsPrimary>(IntoSameContainer, thirdKey)
            .assertClosesWithResultTo<ComposableDestination, ComposableDestinations.PushesToChildAsPrimary>(secondKey)
    }
}