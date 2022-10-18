package dev.enro.core.compose

import dev.enro.core.destinations.*
import org.junit.Test

class ComposableDestinationPresentDialog {

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

    @Test
    fun givenComposableDestination_whenExecutingPresent_andTargetIsDialog_andTargetIsComposableDestination_andDestinationDeliversResult_thenResultIsDelivered() {
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