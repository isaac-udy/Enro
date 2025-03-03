package dev.enro.core.compose

import android.os.Build
import dev.enro.OnlyPassesLocally
import dev.enro.core.destinations.ComposableDestinations
import dev.enro.core.destinations.FragmentDestinations
import dev.enro.core.destinations.assertClosesTo
import dev.enro.core.destinations.assertClosesWithResultTo
import dev.enro.core.destinations.assertPresentsForResultTo
import dev.enro.core.destinations.assertPresentsTo
import dev.enro.core.destinations.launchComposableRoot
import leakcanary.DetectLeaksAfterTestSuccess
import org.junit.Rule
import org.junit.Test

class ComposableDestinationPresentDialog {

    @get:Rule
    val rule = DetectLeaksAfterTestSuccess()

    @Test
    fun givenComposableDestination_whenExecutingPresent_andTargetIsDialog_andTargetIsComposableDestination_thenCorrectDestinationIsOpened() {
        val root = launchComposableRoot()
        root.assertPresentsTo<ComposableDestination, ComposableDestinations.PresentableDialog>()
    }

    @Test
    fun givenComposableDestination_whenExecutingPresent_andTargetIsDialog_andTargetIsComposableDestination_andDestinationIsClosed_thenPreviousDestinationIsActive() {
        val root = launchComposableRoot()
        root.assertPresentsTo<ComposableDestination, ComposableDestinations.PresentableDialog>()
            .assertClosesTo<ComposableDestination, ComposableDestinations.Root>(root.navigation.key)
    }

    @OnlyPassesLocally(
        """
            On API 30, this test seems to fail reliably on emulator.wtf/CI, but passes locally.
        """
    )
    @Test
    fun givenComposableDestination_whenExecutingPresent_andTargetIsDialog_andTargetIsComposableDestination_andDestinationDeliversResult_thenResultIsDelivered() {
        if (Build.VERSION.SDK_INT == 30) {
            return
        }
        val root = launchComposableRoot()
        root.assertPresentsForResultTo<ComposableDestination, ComposableDestinations.PresentableDialog>()
            .assertClosesWithResultTo<ComposableDestination, ComposableDestinations.Root>(root.navigation.key)
    }

    @Test
    fun givenComposableDestination_whenExecutingPresent_andTargetIsDialog_andTargetIsFragmentDestination_thenCorrectDestinationIsOpened() {
        val root = launchComposableRoot()
        root.assertPresentsTo<FragmentDestinations.DialogFragment, FragmentDestinations.PresentableDialog>()
    }

    @Test
    fun givenComposableDestination_whenExecutingPresent_andTargetIsDialog_andTargetIsFragmentDestination_andDestinationIsClosed_thenPreviousDestinationIsActive() {
        val root = launchComposableRoot()
        root.assertPresentsTo<FragmentDestinations.DialogFragment, FragmentDestinations.PresentableDialog>()
            .assertClosesTo<ComposableDestination, ComposableDestinations.Root>(root.navigation.key)
    }

    @Test
    fun givenComposableDestination_whenExecutingPresent_andTargetIsDialog_andTargetIsFragmentDestination_andDestinationDeliversResult_thenResultIsDelivered() {
        val root = launchComposableRoot()
        root.assertPresentsForResultTo<FragmentDestinations.DialogFragment, FragmentDestinations.PresentableDialog>()
            .assertClosesWithResultTo<ComposableDestination, ComposableDestinations.Root>(root.navigation.key)
    }
}