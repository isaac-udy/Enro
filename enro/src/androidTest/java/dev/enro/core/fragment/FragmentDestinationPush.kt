package dev.enro.core.fragment

import dev.enro.core.compose.ComposableDestination
import dev.enro.core.destinations.*
import org.junit.Test

class FragmentDestinationPush {
    @Test
    fun givenFragmentDestination_whenExecutingPush_andTargetIsComposableDestination_thenCorrectDestinationIsOpened() {
        val root = launchFragmentRoot()
        root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoChildContainer)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoSameContainer)
    }

    @Test
    fun givenFragmentDestination_whenExecutingPush_andTargetIsComposableDestination_andDestinationIsClosed_thenPreviousDestinationIsActive() {
        val root = launchFragmentRoot()
        val firstKey = ComposableDestinations.PushesToPrimary("firstKey")
        val secondKey = ComposableDestinations.PushesToPrimary("secondKey")
        root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoChildContainer, firstKey)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoSameContainer, secondKey)
            .assertClosesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(firstKey)
    }

    @Test
    fun givenFragmentDestination_whenExecutingPush_andTargetIsComposableDestination_andDestinationDeliversResult_thenResultIsDelivered() {
        val root = launchFragmentRoot()
        val firstKey = ComposableDestinations.PushesToPrimary()
        val secondKey = ComposableDestinations.PushesToPrimary()
        root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoChildContainer, firstKey)
            .assertPushesForResultTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(
                IntoSameContainer,
                secondKey
            )
            .assertClosesWithResultTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(firstKey)
    }

    @Test
    fun givenFragmentDestination_whenExecutingPush_andTargetIsFragmentDestination_thenCorrectDestinationIsOpened() {
        val root = launchFragmentRoot()
        root.assertPushesTo<FragmentDestinations.Fragment, FragmentDestinations.PushesToPrimary>(IntoChildContainer)
            .assertPushesTo<FragmentDestinations.Fragment, FragmentDestinations.PushesToPrimary>(IntoSameContainer)
    }

    @Test
    fun givenFragmentDestination_whenExecutingPush_andTargetIsFragmentDestination_andDestinationIsClosed_thenPreviousDestinationIsActive() {
        val root = launchFragmentRoot()
        val firstKey = FragmentDestinations.PushesToPrimary()
        val secondKey = FragmentDestinations.PushesToPrimary()
        root.assertPushesTo<FragmentDestinations.Fragment, FragmentDestinations.PushesToPrimary>(IntoChildContainer, firstKey)
            .assertPushesTo<FragmentDestinations.Fragment, FragmentDestinations.PushesToPrimary>(IntoSameContainer, secondKey)
            .assertClosesTo<FragmentDestinations.Fragment, FragmentDestinations.PushesToPrimary>(firstKey)
    }

    @Test
    fun givenFragmentDestination_whenExecutingPush_andTargetIsFragmentDestination_andDestinationDeliversResult_thenResultIsDelivered() {
        val root = launchFragmentRoot()
        val firstKey = FragmentDestinations.PushesToPrimary()
        val secondKey = FragmentDestinations.PushesToPrimary()
        root.assertPushesTo<FragmentDestinations.Fragment, FragmentDestinations.PushesToPrimary>(IntoChildContainer, firstKey)
            .assertPushesForResultTo<FragmentDestinations.Fragment, FragmentDestinations.PushesToPrimary>(
                IntoSameContainer,
                secondKey
            )
            .assertClosesWithResultTo<FragmentDestinations.Fragment, FragmentDestinations.PushesToPrimary>(firstKey)
    }
}