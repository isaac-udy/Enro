package dev.enro.core.fragment

import dev.enro.core.compose.ComposableDestination
import dev.enro.core.destinations.*
import org.junit.Test

class FragmentDestinationPresent {
    @Test
    fun givenFragmentDestination_whenExecutingPresent_andTargetIsComposableDestination_thenCorrectDestinationIsOpened() {
        val root = launchFragmentRoot()

        root.assertPresentsTo<ComposableDestination, ComposableDestinations.Presentable>()
    }

    @Test
    fun givenFragmentDestination_whenExecutingPresent_andTargetIsComposableDestination_andDestinationIsClosed_thenPreviousDestinationIsActive() {
        val root = launchFragmentRoot()
        root.assertPresentsTo<ComposableDestination, ComposableDestinations.Presentable>()
            .assertClosesTo<FragmentDestinations.Fragment, FragmentDestinations.Root>(root.navigation.key)
    }

    @Test
    fun givenFragmentDestination_whenExecutingPresent_andTargetIsComposableDestination_andDestinationDeliversResult_thenResultIsDelivered() {
        val root = launchFragmentRoot()
        root.assertPresentsForResultTo<ComposableDestination, ComposableDestinations.Presentable>()
            .assertClosesWithResultTo<FragmentDestinations.Fragment, FragmentDestinations.Root>(root.navigation.key)
    }

    @Test
    fun givenFragmentDestination_whenExecutingPresent_andTargetIsFragmentDestination_thenCorrectDestinationIsOpened() {
        val root = launchFragmentRoot()

        root.assertPresentsTo<FragmentDestinations.Fragment, FragmentDestinations.Presentable>()
    }

    @Test
    fun givenFragmentDestination_whenExecutingPresent_andTargetIsFragmentDestination_andDestinationIsClosed_thenPreviousDestinationIsActive() {
        val root = launchFragmentRoot()
        root.assertPresentsTo<FragmentDestinations.Fragment, FragmentDestinations.Presentable>()
            .assertClosesTo<FragmentDestinations.Fragment, FragmentDestinations.Root>(root.navigation.key)
    }

    @Test
    fun givenFragmentDestination_whenExecutingPresent_andTargetIsFragmentDestination_andDestinationDeliversResult_thenResultIsDelivered() {
        val root = launchFragmentRoot()
        root.assertPresentsForResultTo<FragmentDestinations.Fragment, FragmentDestinations.Presentable>()
            .assertClosesWithResultTo<FragmentDestinations.Fragment, FragmentDestinations.Root>(root.navigation.key)
    }

    @Test
    fun givenFragmentDestination_whenExecutingPresent_andTargetIsActivityDestination_thenCorrectDestinationIsOpened() {
        val root = launchFragmentRoot()

        root.assertPresentsTo<ActivityDestinations.Activity, ActivityDestinations.Presentable>()
    }

    @Test
    fun givenFragmentDestination_whenExecutingPresent_andTargetIsActivityDestination_andDestinationIsClosed_thenPreviousDestinationIsActive() {
        val root = launchFragmentRoot()
        root.assertPresentsTo<ActivityDestinations.Activity, ActivityDestinations.Presentable>()
            .assertClosesTo<FragmentDestinations.Fragment, FragmentDestinations.Root>(root.navigation.key)
    }

    @Test
    fun givenFragmentDestination_whenExecutingPresent_andTargetIsActivityDestination_andDestinationDeliversResult_thenResultIsDelivered() {
        val root = launchFragmentRoot()
        root.assertPresentsForResultTo<ActivityDestinations.Activity, ActivityDestinations.Presentable>()
            .assertClosesWithResultTo<FragmentDestinations.Fragment, FragmentDestinations.Root>(root.navigation.key)
    }
}