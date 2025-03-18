package dev.enro.core.fragment

import dev.enro.core.compose.ComposableDestination
import dev.enro.core.destinations.*
import leakcanary.DetectLeaksAfterTestSuccess
import org.junit.Rule
import org.junit.Test

class FragmentDestinationPushToChildContainer {

    @get:Rule
    val rule = DetectLeaksAfterTestSuccess()

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

    @Test
    fun givenFragmentDestination_whenExecutingPushToChildContainer_andTargetIsFragmentDestination_thenCorrectDestinationIsOpened() {
        val root = launchFragmentRoot()
        root.assertPushesTo<FragmentDestinations.Fragment, FragmentDestinations.PushesToPrimary>(IntoChildContainer)
            .assertPushesTo<FragmentDestinations.Fragment, FragmentDestinations.PushesToChildAsPrimary>(IntoChildContainer)
    }

    @Test
    fun givenFragmentDestination_whenExecutingMultiplePushesToChildContainer_andTargetIsFragmentDestination_thenCorrectDestinationIsOpened() {
        val root = launchFragmentRoot()
        root.assertPushesTo<FragmentDestinations.Fragment, FragmentDestinations.PushesToPrimary>(IntoChildContainer)
            .assertPushesTo<FragmentDestinations.Fragment, FragmentDestinations.PushesToChildAsPrimary>(IntoChildContainer)
            .assertPushesTo<FragmentDestinations.Fragment, FragmentDestinations.PushesToChildAsPrimary>(IntoSameContainer)
    }

    @Test
    fun givenFragmentDestination_whenExecutingPushToChildContainer_andTargetIsFragmentDestination_andDestinationIsClosed_thenPreviousDestinationIsActive() {
        val root = launchFragmentRoot()
        val firstKey = FragmentDestinations.PushesToPrimary()
        val secondKey = FragmentDestinations.PushesToChildAsPrimary()
        root.assertPushesTo<FragmentDestinations.Fragment, FragmentDestinations.PushesToPrimary>(IntoChildContainer, firstKey)
            .assertPushesTo<FragmentDestinations.Fragment, FragmentDestinations.PushesToChildAsPrimary>(IntoChildContainer, secondKey)
            .assertClosesTo<FragmentDestinations.Fragment, FragmentDestinations.PushesToPrimary>(firstKey)
    }

    @Test
    fun givenFragmentDestination_whenExecutingMultiplePushesToChildContainer_andTargetIsFragmentDestination_andDestinationIsClosed_thenPreviousDestinationIsActive() {
        val root = launchFragmentRoot()
        val firstKey = FragmentDestinations.PushesToPrimary()
        val secondKey = FragmentDestinations.PushesToChildAsPrimary()
        val thirdKey = FragmentDestinations.PushesToChildAsPrimary()
        root.assertPushesTo<FragmentDestinations.Fragment, FragmentDestinations.PushesToPrimary>(IntoChildContainer, firstKey)
            .assertPushesTo<FragmentDestinations.Fragment, FragmentDestinations.PushesToChildAsPrimary>(IntoChildContainer, secondKey)
            .assertPushesTo<FragmentDestinations.Fragment, FragmentDestinations.PushesToChildAsPrimary>(IntoSameContainer, thirdKey)
            .assertClosesTo<FragmentDestinations.Fragment, FragmentDestinations.PushesToChildAsPrimary>(secondKey)
    }

    @Test
    fun givenFragmentDestination_whenExecutingPushToChildContainer_andTargetIsFragmentDestination_andDestinationDeliversResult_thenResultIsDelivered() {
        val root = launchFragmentRoot()
        val firstKey = FragmentDestinations.PushesToPrimary()
        val secondKey = FragmentDestinations.PushesToChildAsPrimary()
        root.assertPushesTo<FragmentDestinations.Fragment, FragmentDestinations.PushesToPrimary>(IntoChildContainer, firstKey)
            .assertPushesForResultTo<FragmentDestinations.Fragment, FragmentDestinations.PushesToChildAsPrimary>(IntoChildContainer, secondKey)
            .assertClosesWithResultTo<FragmentDestinations.Fragment, FragmentDestinations.PushesToPrimary>(firstKey)
    }

    @Test
    fun givenFragmentDestination_whenExecutingMultiplePushesToChildContainer_andTargetIsFragmentDestination_andDestinationDeliversResult_thenResultIsDelivered() {
        val root = launchFragmentRoot()
        val firstKey = FragmentDestinations.PushesToPrimary()
        val secondKey = FragmentDestinations.PushesToChildAsPrimary()
        val thirdKey = FragmentDestinations.PushesToChildAsPrimary()
        root.assertPushesTo<FragmentDestinations.Fragment, FragmentDestinations.PushesToPrimary>(IntoChildContainer, firstKey)
            .assertPushesTo<FragmentDestinations.Fragment, FragmentDestinations.PushesToChildAsPrimary>(IntoChildContainer, secondKey)
            .assertPushesForResultTo<FragmentDestinations.Fragment, FragmentDestinations.PushesToChildAsPrimary>(IntoSameContainer, thirdKey)
            .assertClosesWithResultTo<FragmentDestinations.Fragment, FragmentDestinations.PushesToChildAsPrimary>(secondKey)
    }
}