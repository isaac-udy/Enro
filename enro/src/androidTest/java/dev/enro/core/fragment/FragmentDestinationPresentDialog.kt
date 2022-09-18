package dev.enro.core.fragment

import dev.enro.core.compose.ComposableDestination
import dev.enro.core.destinations.*
import org.junit.Test

class FragmentDestinationPresentDialog {

    @Test
    fun givenFragmentDestination_whenExecutingPresent_andTargetIsDialog_andTargetIsComposableDestination_thenCorrectDestinationIsOpened() {
        val root = launchFragmentRoot()
        root.assertPresentsTo<ComposableDestination, ComposableDestinations.PresentableDialog>()
    }

    @Test
    fun givenFragmentDestination_whenExecutingPresent_andTargetIsDialog_andTargetIsComposableDestination_andDestinationIsClosed_thenPreviousDestinationIsActive() {
        val root = launchFragmentRoot()
        root.assertPresentsTo<ComposableDestination, ComposableDestinations.PresentableDialog>()
            .assertClosesTo<FragmentDestinations.Fragment, FragmentDestinations.Root>(root.navigation.key)
    }

    @Test
    fun givenFragmentDestination_whenExecutingPresent_andTargetIsDialog_andTargetIsComposableDestination_andDestinationDeliversResult_thenResultIsDelivered() {
        val root = launchFragmentRoot()
        root.assertPresentsForResultTo<ComposableDestination, ComposableDestinations.PresentableDialog>()
            .assertClosesWithResultTo<FragmentDestinations.Fragment, FragmentDestinations.Root>(root.navigation.key)
    }

    @Test
    fun givenFragmentDestination_whenExecutingPresent_andTargetIsDialog_andTargetIsFragmentDestination_thenCorrectDestinationIsOpened() {
        val root = launchFragmentRoot()
        root.assertPresentsTo<FragmentDestinations.DialogFragment, FragmentDestinations.PresentableDialog>()
    }

    @Test
    fun givenFragmentDestination_whenExecutingPresent_andTargetIsDialog_andTargetIsFragmentDestination_andDestinationIsClosed_thenPreviousDestinationIsActive() {
        val root = launchFragmentRoot()
        root.assertPresentsTo<FragmentDestinations.DialogFragment, FragmentDestinations.PresentableDialog>()
            .assertClosesTo<FragmentDestinations.Fragment, FragmentDestinations.Root>(root.navigation.key)
    }

    @Test
    fun givenFragmentDestination_whenExecutingPresent_andTargetIsDialog_andTargetIsFragmentDestination_andDestinationDeliversResult_thenResultIsDelivered() {
        val root = launchFragmentRoot()
        root.assertPresentsForResultTo<FragmentDestinations.DialogFragment, FragmentDestinations.PresentableDialog>()
            .assertClosesWithResultTo<FragmentDestinations.Fragment, FragmentDestinations.Root>(root.navigation.key)
    }
}