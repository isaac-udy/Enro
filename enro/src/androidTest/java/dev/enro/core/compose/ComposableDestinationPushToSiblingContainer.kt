package dev.enro.core.compose

import dev.enro.core.container.setActive
import dev.enro.core.destinations.*
import leakcanary.DetectLeaksAfterTestSuccess
import org.junit.Rule
import org.junit.Test

class ComposableDestinationPushToSiblingContainer {

    @get:Rule
    val rule = DetectLeaksAfterTestSuccess()

    @Test
    fun givenComposableDestination_whenExecutingPushToSiblingContainer_andTargetIsComposableDestination_thenCorrectDestinationIsOpened() {
        val root = launchComposableRoot()
        root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(
            IntoChildContainer
        )
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToSecondary>(
                IntoSiblingContainer
            )
    }

    @Test
    fun givenComposableDestination_whenExecutingPushToSiblingContainer_andTargetIsComposableDestination_andSiblingPushesAgain_thenCorrectDestinationIsOpened() {
        val root = launchComposableRoot()
        root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(
            IntoChildContainer
        )
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToSecondary>(
                IntoSiblingContainer
            )
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToSecondary>(
                IntoSameContainer
            )
    }

    @Test
    fun givenComposableDestination_whenExecutingPushToSiblingContainer_andTargetIsComposableDestination_andDestinationIsClosed_thenPreviousDestinationIsActive() {
        val root = launchComposableRoot()
        val firstKey = ComposableDestinations.PushesToPrimary()
        val secondKey = ComposableDestinations.PushesToSecondary()
        root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(
            IntoChildContainer, firstKey)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToSecondary>(
                IntoSiblingContainer, secondKey)
            .assertClosesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(firstKey)
    }

    @Test
    fun givenComposableDestination_whenExecutingMultiplePushesToSiblingContainer_andTargetIsComposableDestination_andDestinationIsClosed_thenPreviousDestinationIsActive() {
        val root = launchComposableRoot()
        val expectedClose = ComposableDestinations.PushesToSecondary()
        root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(
            IntoChildContainer
        )
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToSecondary>(
                IntoSiblingContainer, expectedClose)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToSecondary>(
                IntoSameContainer
            )
            .assertClosesTo<ComposableDestination, ComposableDestinations.PushesToSecondary>(expectedClose)
    }

    @Test
    fun givenComposableDestination_whenExecutingPushToSiblingContainer_andTargetIsComposableDestination_andDestinationDeliversResult_thenResultIsDelivered() {
        val root = launchComposableRoot()
        val firstKey = ComposableDestinations.PushesToPrimary()
        val secondKey = ComposableDestinations.PushesToSecondary()
        root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(
            IntoChildContainer, firstKey)
            .assertPushesForResultTo<ComposableDestination, ComposableDestinations.PushesToSecondary>(
                IntoSiblingContainer, secondKey)
            .assertClosesWithResultTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(firstKey)
    }

    @Test
    fun givenComposableDestination_whenExecutingMultiplePushesToSiblingContainer_andTargetIsComposableDestination_andDestinationDeliversResult_thenResultIsDelivered() {
        val root = launchComposableRoot()
        val firstKey = ComposableDestinations.PushesToPrimary()
        val secondKey = ComposableDestinations.PushesToSecondary()

        val primary = root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(
            IntoChildContainer, firstKey)
        val primaryContainer = root.navigationContext.containerManager.activeContainer

        primary.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToSecondary>(
            IntoSiblingContainer
        )
        primary.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToSecondary>(
            IntoSiblingContainer
        )
        primary.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToSecondary>(
            IntoSiblingContainer
        )

        primaryContainer?.setActive() // TODO Should this be necessary? When a result is delivered, should that container automatically become active?

        primary.assertPushesForResultTo<ComposableDestination, ComposableDestinations.PushesToSecondary>(
            IntoSiblingContainer, secondKey)
            .assertClosesWithResultTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(firstKey)
    }
}