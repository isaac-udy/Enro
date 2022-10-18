package dev.enro.core.compose

import dev.enro.core.destinations.ComposableDestinations
import dev.enro.core.destinations.IntoChildContainer
import dev.enro.core.destinations.assertPushesTo
import dev.enro.core.destinations.launchComposableRoot
import org.junit.Test

class ManuallyBoundComposableTest {
    @Test
    fun givenManuallyDefinedComposable_whenComposableIsAsRootOfNavigation_thenCorrectComposableIsDisplayed() {
        val root = launchComposableRoot()

        root.assertPushesTo<ComposableDestination, ComposableDestinations.ManuallyBound>(IntoChildContainer)
    }

}