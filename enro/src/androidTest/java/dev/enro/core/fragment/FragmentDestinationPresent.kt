package dev.enro.core.fragment

import android.os.Parcelable
import androidx.fragment.app.Fragment
import dev.enro.destination.compose.ComposableDestination
import dev.enro.core.destinations.*
import junit.framework.TestCase
import kotlinx.parcelize.Parcelize
import leakcanary.DetectLeaksAfterTestSuccess
import org.junit.Rule
import org.junit.Test
import java.util.*

class FragmentDestinationPresent {
    @get:Rule
    val rule = DetectLeaksAfterTestSuccess()

    @Test
    fun givenFragmentDestination_whenExecutingPresent_andTargetIsComposableDestination_thenCorrectDestinationIsOpened() {
        val root = launchFragmentRoot()

        root.assertPresentsTo<ComposableDestination, ComposableDestinations.Presentable>()
    }

    @Parcelize
    data class ParcelableForTest(
        val parcelableId: String
    ) : Parcelable

    @Test
    fun givenFragmentDestination_whenExecutingPresent_andTargetIsGenericFragmentDestination_thenCorrectDestinationIsOpened() {
        val root = launchFragmentRoot()
        val expectedKey = FragmentDestinations.Generic(ParcelableForTest(UUID.randomUUID().toString()))

        val context = root.assertPresentsTo<Fragment, FragmentDestinations.Generic<ParcelableForTest>>(expectedKey)
        TestCase.assertEquals(expectedKey, context.navigation.key)
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