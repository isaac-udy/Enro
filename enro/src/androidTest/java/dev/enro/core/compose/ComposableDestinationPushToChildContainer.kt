package dev.enro.core.compose

import androidx.activity.ComponentActivity
import dev.enro.core.close
import dev.enro.core.destinations.*
import dev.enro.expectActivity
import dev.enro.expectComposableContext
import org.junit.Test

class ComposableDestinationPushToChildContainer {
    @Test
    fun givenComposableDestination_whenExecutingPushToChildContainer_andTargetIsComposableDestination_thenCorrectDestinationIsOpened() {
         val root = launchComposableRoot()
        root
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(IntoChildContainer)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToChildAsPrimary>(IntoChildContainer)
    }

    @Test
    fun givenComposableDestination_whenExecutingMultiplePushesToChildContainer_andTargetIsComposableDestination_thenCorrectDestinationIsOpened() {
        val root = launchComposableRoot()
        root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(
            IntoChildContainer
        )
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToChildAsPrimary>(
                IntoChildContainer
            )
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToChildAsPrimary>(
                IntoSameContainer
            )
    }


    // Test name is too long with expected conditions:
    // Given a Composable destination that pushes multiple children, when the activity is recreated, then the children should be restored correctly and the backstack should be maintained
    @Test
    fun givenComposableDestination_whenExecutingMultiplePushesToChildContainer_andTargetIsComposableDestination_andRecreated_thenEverythingWorks() {
        val root = launchComposableRoot()
        root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(
            IntoChildContainer
        )
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToChildAsPrimary>(
                IntoChildContainer,
                ComposableDestinations.PushesToChildAsPrimary("First")
            )
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToChildAsPrimary>(
                IntoSameContainer,
                ComposableDestinations.PushesToChildAsPrimary("Second")
            )
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToChildAsPrimary>(
                IntoSameContainer,
                ComposableDestinations.PushesToChildAsPrimary("Third")
            )
        expectActivity<ComponentActivity>().apply {
            runOnUiThread {
                recreate()
            }
        }
        expectActivity<ComponentActivity>()
        expectComposableContext<ComposableDestinations.Root>()
        expectComposableContext<ComposableDestinations.PushesToChildAsPrimary> { it.navigation.key.id == "Third" }
            .navigation.close()
        expectComposableContext<ComposableDestinations.PushesToChildAsPrimary> { it.navigation.key.id == "Second" }
            .navigation.close()
        expectComposableContext<ComposableDestinations.PushesToChildAsPrimary> { it.navigation.key.id == "First" }
    }

    @Test
    fun givenComposableDestination_whenExecutingPushToChildContainer_andTargetIsComposableDestination_andDestinationIsClosed_thenPreviousDestinationIsActive() {
        val root = launchComposableRoot()
        val firstKey = ComposableDestinations.PushesToPrimary()
        val secondKey = ComposableDestinations.PushesToChildAsPrimary()
        root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(
            IntoChildContainer, firstKey)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToChildAsPrimary>(
                IntoChildContainer, secondKey)
            .assertClosesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(firstKey)
    }

    @Test
    fun givenComposableDestination_whenExecutingMultiplePushesToChildContainer_andTargetIsComposableDestination_andDestinationIsClosed_thenPreviousDestinationIsActive() {
        val root = launchComposableRoot()
        val firstKey = ComposableDestinations.PushesToPrimary()
        val secondKey = ComposableDestinations.PushesToChildAsPrimary()
        val thirdKey = ComposableDestinations.PushesToChildAsPrimary()
        root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(
            IntoChildContainer, firstKey)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToChildAsPrimary>(
                IntoChildContainer, secondKey)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToChildAsPrimary>(
                IntoSameContainer, thirdKey)
            .assertClosesTo<ComposableDestination, ComposableDestinations.PushesToChildAsPrimary>(secondKey)
    }

    @Test
    fun givenComposableDestination_whenExecutingPushToChildContainer_andTargetIsComposableDestination_andDestinationDeliversResult_thenResultIsDelivered() {
         val root = launchComposableRoot()
        val firstKey = ComposableDestinations.PushesToPrimary()
        val secondKey = ComposableDestinations.PushesToChildAsPrimary()
        root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(
            IntoChildContainer, firstKey)
            .assertPushesForResultTo<ComposableDestination, ComposableDestinations.PushesToChildAsPrimary>(
                IntoChildContainer, secondKey)
            .assertClosesWithResultTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(firstKey)
    }

    @Test
    fun givenComposableDestination_whenExecutingMultiplePushesToChildContainer_andTargetIsComposableDestination_andDestinationDeliversResult_thenResultIsDelivered() {
        val root = launchComposableRoot()
        val firstKey = ComposableDestinations.PushesToPrimary()
        val secondKey = ComposableDestinations.PushesToChildAsPrimary()
        val thirdKey = ComposableDestinations.PushesToChildAsPrimary()
        root.assertPushesTo<ComposableDestination, ComposableDestinations.PushesToPrimary>(
            IntoChildContainer, firstKey)
            .assertPushesTo<ComposableDestination, ComposableDestinations.PushesToChildAsPrimary>(
                IntoChildContainer, secondKey)
            .assertPushesForResultTo<ComposableDestination, ComposableDestinations.PushesToChildAsPrimary>(
                IntoSameContainer, thirdKey)
            .assertClosesWithResultTo<ComposableDestination, ComposableDestinations.PushesToChildAsPrimary>(secondKey)
    }
}