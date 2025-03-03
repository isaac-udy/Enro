package dev.enro.core.compose

import androidx.fragment.app.Fragment
import androidx.test.espresso.Espresso
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.core.compose.container.ComposableNavigationContainer
import dev.enro.core.containerManager
import dev.enro.core.destinations.*
import dev.enro.core.directParentContainer
import dev.enro.expectContext
import dev.enro.expectNoComposableContext
import leakcanary.DetectLeaksAfterTestSuccess
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ComposableDestinationPush {

    @get:Rule
    val rule = DetectLeaksAfterTestSuccess()

    @Test
    fun givenComposableDestination_whenExecutingPush_andTargetIsComposableDestination_thenCorrectDestinationIsOpened() {
        val root = launchComposableRoot()
        root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(
            IntoChildContainer
        )
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(
                IntoSameContainer
            )
    }

    @Test
    fun givenComposableDestination_whenExecutingMultiplePushes_andBackNavigation_andContainerIsSaved_thenEverythingWorksFine() {
        val root = launchComposableRoot()
        val first = ComposableDestinations.PushesToPrimary()
        val second = ComposableDestinations.PushesToPrimary()
        val third = ComposableDestinations.PushesToPrimary()
        root
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoChildContainer, first)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoSameContainer, second)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoSameContainer, third)
            .assertClosesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(second)
            .assertClosesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(first)

        expectNoComposableContext<ComposableDestinations.PushesToPrimary> {
            it.navigation.key == second || it.navigation.key == third
        }
        root.context.containerManager.activeContainer?.save()
    }

    @Test
    fun givenComposableDestination_whenExecutingPush_andTargetIsComposableDestination_andDestinationIsClosed_thenPreviousDestinationIsActive() {
        val root = launchComposableRoot()
        val firstKey = ComposableDestinations.PushesToPrimary()
        val secondKey = ComposableDestinations.PushesToPrimary()
        root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(
            IntoChildContainer, firstKey)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(
                IntoSameContainer, secondKey)
            .assertClosesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(firstKey)
    }

    @Test
    fun givenComposableDestination_whenExecutingPush_andTargetIsComposableDestination_andDestinationDeliversResult_thenResultIsDelivered() {
        val root = launchComposableRoot()
        val firstKey = ComposableDestinations.PushesToPrimary()
        val secondKey = ComposableDestinations.PushesToPrimary()
        root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoChildContainer, firstKey)
            .assertPushesForResultTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoSameContainer, secondKey)
            .assertClosesWithResultTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(firstKey)
    }

    @OptIn(AdvancedEnroApi::class)
    @Test
    fun givenComposableRootDestination_whenPushingComposables_thenComposablesArePushedIntoComposableContainerNotAsFragments() {
        val root = launchComposableRoot()
        val composableContainer = root.navigationContext.directParentContainer()
        assertTrue(composableContainer is ComposableNavigationContainer)
        /**
         * When a composables is launched into the root of an activity, the first composable container should accept all navigation keys,
         * and allow additional composable pushes within that container, rather than wrapping each composable in a Fragment Host.
         *
         * This checks that the composable destinations are opened, but also explicitly ensures that they are all opened into
         * exactly the same composable container, rather than just being opened "IntoSameContainer", which allows for destinations
         * to be opened into the same container while hosted in some other context type.
         */
        root.assertPushesTo<ComposableDestination, ComposableDestinations.Pushable>(IntoSameContainer)
            .also { assertEquals(composableContainer, it.navigationContext.directParentContainer()) }
            .assertPushesTo<ComposableDestination, ComposableDestinations.Pushable>(IntoSameContainer)
            .also { assertEquals(composableContainer, it.navigationContext.directParentContainer()) }
            .assertPushesTo<ComposableDestination, ComposableDestinations.Pushable>(IntoSameContainer)
            .also { assertEquals(composableContainer, it.navigationContext.directParentContainer()) }
    }

    @Test
    fun givenComposableRootDestination_whenPushingComposablesAndFragments_thenClosingFragmentsMaintainsComposableState() {
        val root = launchComposableRoot()

        val firstComposable = ComposableDestinations.Pushable()
        val secondComposable = ComposableDestinations.Pushable()
        val thirdComposable = ComposableDestinations.Pushable()
        val fourthComposable = ComposableDestinations.Pushable()
        val fifthComposable = ComposableDestinations.Pushable()
        val firstFragment = FragmentDestinations.Pushable()
        val secondFragment = FragmentDestinations.Pushable()

        root.assertPushesTo<ComposableDestination, ComposableDestinations.Pushable>(IntoSameContainer, firstComposable)
            .assertPushesTo<ComposableDestination, ComposableDestinations.Pushable>(IntoSameContainer, secondComposable)
            .assertPushesTo<ComposableDestination, ComposableDestinations.Pushable>(IntoSameContainer, thirdComposable)
            .assertPushesTo<ComposableDestination, ComposableDestinations.Pushable>(IntoSameContainer, fourthComposable)
            .assertPushesTo<ComposableDestination, ComposableDestinations.Pushable>(IntoSameContainer, fifthComposable)
            .assertPushesTo<Fragment, FragmentDestinations.Pushable>(IntoSameContainer, firstFragment)
            .assertPushesTo<Fragment, FragmentDestinations.Pushable>(IntoSameContainer, secondFragment)

            .assertClosesTo<Fragment, FragmentDestinations.Pushable>(firstFragment)
            .assertClosesTo<ComposableDestination, ComposableDestinations.Pushable>(fifthComposable)
            .assertClosesTo<ComposableDestination, ComposableDestinations.Pushable>(fourthComposable)
            .assertClosesTo<ComposableDestination, ComposableDestinations.Pushable>(thirdComposable)
            .assertClosesTo<ComposableDestination, ComposableDestinations.Pushable>(secondComposable)
            .assertClosesTo<ComposableDestination, ComposableDestinations.Pushable>(firstComposable)
    }

    @Test
    fun givenComposableRootDestination_whenPushingComposablesAndFragments_thenClosingFragmentsWithBackButtonMaintainsComposableState() {
        val root = launchComposableRoot()

        val firstComposable = ComposableDestinations.Pushable()
        val secondComposable = ComposableDestinations.Pushable()
        val thirdComposable = ComposableDestinations.Pushable()
        val fourthComposable = ComposableDestinations.Pushable()
        val fifthComposable = ComposableDestinations.Pushable()
        val firstFragment = FragmentDestinations.Pushable()
        val secondFragment = FragmentDestinations.Pushable()

        root.assertPushesTo<ComposableDestination, ComposableDestinations.Pushable>(IntoSameContainer, firstComposable)
            .assertPushesTo<ComposableDestination, ComposableDestinations.Pushable>(IntoSameContainer, secondComposable)
            .assertPushesTo<ComposableDestination, ComposableDestinations.Pushable>(IntoSameContainer, thirdComposable)
            .assertPushesTo<ComposableDestination, ComposableDestinations.Pushable>(IntoSameContainer, fourthComposable)
            .assertPushesTo<ComposableDestination, ComposableDestinations.Pushable>(IntoSameContainer, fifthComposable)
            .assertPushesTo<Fragment, FragmentDestinations.Pushable>(IntoSameContainer, firstFragment)
            .assertPushesTo<Fragment, FragmentDestinations.Pushable>(IntoSameContainer, secondFragment)

        Espresso.pressBack()
        expectContext<Fragment, FragmentDestinations.Pushable> { it.navigation.key == firstFragment}

        Espresso.pressBack()
        expectContext<ComposableDestination, ComposableDestinations.Pushable> { it.navigation.key == fifthComposable}

        Espresso.pressBack()
        expectContext<ComposableDestination, ComposableDestinations.Pushable> { it.navigation.key == fourthComposable}

        Espresso.pressBack()
        expectContext<ComposableDestination, ComposableDestinations.Pushable> { it.navigation.key == thirdComposable}

        Espresso.pressBack()
        expectContext<ComposableDestination, ComposableDestinations.Pushable> { it.navigation.key == secondComposable}

        Espresso.pressBack()
        expectContext<ComposableDestination, ComposableDestinations.Pushable> { it.navigation.key == firstComposable}
    }
}