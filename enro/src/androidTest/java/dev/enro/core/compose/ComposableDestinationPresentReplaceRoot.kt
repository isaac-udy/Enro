package dev.enro.core.compose

import dev.enro.core.destinations.*
import dev.enro.destination.compose.ComposableDestination
import leakcanary.DetectLeaksAfterTestSuccess
import org.junit.Rule
import org.junit.Test

class ComposableDestinationPresentReplaceRoot {

    @get:Rule
    val rule = DetectLeaksAfterTestSuccess()

    @Test
    fun givenComposableDestination_whenExecutingReplaceRoot_andTargetIsComposableDestination_thenCorrectDestinationIsOpened() {
        val root = launchComposableRoot()
        root.assertReplacesRootTo<ComposableDestination, ComposableDestinations.Presentable>()
    }

    @Test
    fun givenComposableDestination_whenExecutingReplaceRoot_andTargetIsComposableDestination_andDestinationIsClosed_thenNoDestinationIsActive() {
        val root = launchComposableRoot()
        root.assertReplacesRootTo<ComposableDestination, ComposableDestinations.Presentable>()
            .assertClosesToNothing()
    }

    @Test
    fun givenComposableDestination_whenExecutingReplaceRoot_andTargetIsFragmentDestination_thenCorrectDestinationIsOpened() {
        val root = launchComposableRoot()
        root.assertReplacesRootTo<FragmentDestinations.Fragment, FragmentDestinations.Presentable>()
    }

    @Test
    fun givenComposableDestination_whenExecutingReplaceRoot_andTargetIsFragmentDestination_andDestinationIsClosed_thenNoDestinationIsActive() {
        val root = launchComposableRoot()
        root.assertReplacesRootTo<FragmentDestinations.Fragment, FragmentDestinations.Presentable>()
            .assertClosesToNothing()
    }

    @Test
    fun givenComposableDestination_whenExecutingReplaceRoot_andTargetIsActivityDestination_thenCorrectDestinationIsOpened() {
        val root = launchComposableRoot()
        root.assertReplacesRootTo<ActivityDestinations.Activity, ActivityDestinations.Presentable>()
    }

    @Test
    fun givenComposableDestination_whenExecutingReplaceRoot_andTargetIsActivityDestination_andDestinationIsClosed_thenNoDestinationIsActive() {
        val root = launchComposableRoot()
        root.assertReplacesRootTo<ActivityDestinations.Activity, ActivityDestinations.Presentable>()
            .assertClosesToNothing()
    }
}