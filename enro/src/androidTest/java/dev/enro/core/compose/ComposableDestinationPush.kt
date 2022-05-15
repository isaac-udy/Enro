package dev.enro.core.compose

import dev.enro.core.destinations.*
import org.junit.Test

class ComposableDestinationPush {
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
        root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(
            IntoChildContainer, firstKey)
            .assertPushesForResultTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(
                IntoSameContainer, secondKey)
            .assertClosesWithResultTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(firstKey)
    }
}