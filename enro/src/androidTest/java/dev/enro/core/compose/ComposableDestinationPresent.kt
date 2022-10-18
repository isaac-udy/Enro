package dev.enro.core.compose

import dev.enro.core.destinations.*
import org.junit.Test

class ComposableDestinationPresent {
    @Test
    fun givenComposableDestination_whenExecutingPresent_andTargetIsComposableDestination_thenCorrectDestinationIsOpened() {
        val root = launchComposableRoot()

        root.assertPresentsTo<ComposableDestination, ComposableDestinations.Presentable>()
    }

    @Test
    fun givenComposableDestination_whenExecutingPresent_andTargetIsComposableDestination_andDestinationIsClosed_thenPreviousDestinationIsActive() {
        val root = launchComposableRoot()
        root.assertPresentsTo<ComposableDestination, ComposableDestinations.Presentable>()
            .assertClosesTo<ComposableDestination, ComposableDestinations.Root>(root.navigation.key)
    }

    @Test
    fun givenComposableDestination_whenExecutingPresent_andTargetIsComposableDestination_andDestinationDeliversResult_thenResultIsDelivered() {
        val root = launchComposableRoot()
        root.assertPresentsForResultTo<ComposableDestination, ComposableDestinations.Presentable>()
            .assertClosesWithResultTo<ComposableDestination, ComposableDestinations.Root>(root.navigation.key)
    }

    @Test
    fun givenComposableDestination_whenExecutingPresent_andTargetIsFragmentDestination_thenCorrectDestinationIsOpened() {
        val root = launchComposableRoot()

        root.assertPresentsTo<FragmentDestinations.Fragment, FragmentDestinations.Presentable>()
    }

    @Test
    fun givenComposableDestination_whenExecutingPresent_andTargetIsFragmentDestination_andDestinationIsClosed_thenPreviousDestinationIsActive() {
        val root = launchComposableRoot()
        root.assertPresentsTo<FragmentDestinations.Fragment, FragmentDestinations.Presentable>()
            .assertClosesTo<ComposableDestination, ComposableDestinations.Root>(root.navigation.key)
    }

    @Test
    fun givenComposableDestination_whenExecutingPresent_andTargetIsFragmentDestination_andDestinationDeliversResult_thenResultIsDelivered() {
        val root = launchComposableRoot()
        root.assertPresentsForResultTo<FragmentDestinations.Fragment, FragmentDestinations.Presentable>()
            .assertClosesWithResultTo<ComposableDestination, ComposableDestinations.Root>(root.navigation.key)
    }

    @Test
    fun givenComposableDestination_whenExecutingPresent_andTargetIsActivityDestination_thenCorrectDestinationIsOpened() {
        val root = launchComposableRoot()

        root.assertPresentsTo<ActivityDestinations.Activity, ActivityDestinations.Presentable>()
    }

    @Test
    fun givenComposableDestination_whenExecutingPresent_andTargetIsActivityDestination_andDestinationIsClosed_thenPreviousDestinationIsActive() {
        val root = launchComposableRoot()
        root.assertPresentsTo<ActivityDestinations.Activity, ActivityDestinations.Presentable>()
            .assertClosesTo<ComposableDestination, ComposableDestinations.Root>(root.navigation.key)
    }

    @Test
    fun givenComposableDestination_whenExecutingPresent_andTargetIsActivityDestination_andDestinationDeliversResult_thenResultIsDelivered() {
        val root = launchComposableRoot()
        root.assertPresentsForResultTo<ActivityDestinations.Activity, ActivityDestinations.Presentable>()
            .assertClosesWithResultTo<ComposableDestination, ComposableDestinations.Root>(root.navigation.key)
    }
}