package dev.enro.core.fragment

import dev.enro.core.compose.ComposableDestination
import dev.enro.core.destinations.*
import leakcanary.DetectLeaksAfterTestSuccess
import org.junit.Rule
import org.junit.Test

class FragmentDestinationPresentReplaceRoot {

    @get:Rule
    val rule = DetectLeaksAfterTestSuccess()

    @Test
    fun givenFragmentDestination_whenExecutingReplaceRoot_andTargetIsComposableDestination_thenCorrectDestinationIsOpened() {
        val root = launchFragmentRoot()
        root.assertReplacesRootTo<ComposableDestination, ComposableDestinations.Presentable>()
    }

    @Test
    fun givenFragmentDestination_whenExecutingReplaceRoot_andTargetIsComposableDestination_andDestinationIsClosed_thenNoDestinationIsActive() {
        val root = launchFragmentRoot()
        root.assertReplacesRootTo<ComposableDestination, ComposableDestinations.Presentable>()
            .assertClosesToNothing()
    }

    @Test
    fun givenFragmentDestination_whenExecutingReplaceRoot_andTargetIsFragmentDestination_thenCorrectDestinationIsOpened() {
        val root = launchFragmentRoot()
        root.assertReplacesRootTo<FragmentDestinations.Fragment, FragmentDestinations.Presentable>()
    }

    @Test
    fun givenFragmentDestination_whenExecutingReplaceRoot_andTargetIsFragmentDestination_andDestinationIsClosed_thenNoDestinationIsActive() {
        val root = launchFragmentRoot()
        root.assertReplacesRootTo<FragmentDestinations.Fragment, FragmentDestinations.Presentable>()
            .assertClosesToNothing()
    }

    @Test
    fun givenFragmentDestination_whenExecutingReplaceRoot_andTargetIsActivityDestination_thenCorrectDestinationIsOpened() {
        val root = launchFragmentRoot()
        root.assertReplacesRootTo<ActivityDestinations.Activity, ActivityDestinations.Presentable>()
    }

    @Test
    fun givenFragmentDestination_whenExecutingReplaceRoot_andTargetIsActivityDestination_andDestinationIsClosed_thenNoDestinationIsActive() {
        val root = launchFragmentRoot()
        root.assertReplacesRootTo<ActivityDestinations.Activity, ActivityDestinations.Presentable>()
            .assertClosesToNothing()
    }
}