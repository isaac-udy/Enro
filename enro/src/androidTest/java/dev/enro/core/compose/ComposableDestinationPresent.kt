package dev.enro.core.compose

import android.os.Parcelable
import dev.enro.core.destinations.*
import junit.framework.TestCase.assertEquals
import kotlinx.parcelize.Parcelize
import leakcanary.DetectLeaksAfterTestSuccess
import org.junit.Rule
import org.junit.Test
import java.util.*

class ComposableDestinationPresent {
    @get:Rule
    val rule = DetectLeaksAfterTestSuccess()

    @Test
    fun givenComposableDestination_whenExecutingPresent_andTargetIsComposableDestination_thenCorrectDestinationIsOpened() {
        val root = launchComposableRoot()

        root.assertPresentsTo<ComposableDestination, ComposableDestinations.Presentable>()
    }

    @Parcelize
    data class ParcelableForTest(
        val parcelableId: String
    ) : Parcelable

    @Test
    fun givenComposableDestination_whenExecutingPresent_andTargetIsGenericComposableDestination_thenCorrectDestinationIsOpened() {
        val root = launchComposableRoot()
        val expectedKey = ComposableDestinations.Generic(ParcelableForTest(UUID.randomUUID().toString()))

        val context = root.assertPresentsTo<ComposableDestination, ComposableDestinations.Generic<ParcelableForTest>>(expectedKey)
        assertEquals(expectedKey, context.navigation.key)
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