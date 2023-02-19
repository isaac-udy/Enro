package dev.enro.core.compose

import dev.enro.core.destinations.ComposableDestinations
import dev.enro.core.destinations.IntoChildContainer
import dev.enro.core.destinations.assertPushesTo
import dev.enro.core.destinations.launchComposableRoot
import leakcanary.DetectLeaksAfterTestSuccess
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test

class ManuallyBoundComposableTest {
    @get:Rule
    val rule = DetectLeaksAfterTestSuccess()

    @Test
    fun givenManuallyDefinedComposable_whenComposableIsAsRootOfNavigation_thenCorrectComposableIsDisplayed() {
        val root = launchComposableRoot()

        root.assertPushesTo<ComposableDestination, ComposableDestinations.ManuallyBound>(IntoChildContainer)
    }

    @Test
    fun givenManuallyDefinedComposable_whenManuallyDefinedComposableIsPushedMultipleTimes_thenTheDestinationIsUnique() {
        val root = launchComposableRoot()

        val first = root.assertPushesTo<ComposableDestination, ComposableDestinations.ManuallyBound>(IntoChildContainer)
        val second = root.assertPushesTo<ComposableDestination, ComposableDestinations.ManuallyBound>(IntoChildContainer)
        val third = root.assertPushesTo<ComposableDestination, ComposableDestinations.ManuallyBound>(IntoChildContainer)

        assertNotEquals(first.navigationContext, second.navigationContext)
        assertNotEquals(first.navigationContext.contextReference, second.navigationContext.contextReference)

        assertNotEquals(first.navigationContext, third.navigationContext)
        assertNotEquals(first.navigationContext.contextReference, third.navigationContext.contextReference)

        assertNotEquals(second.navigationContext, third.navigationContext)
        assertNotEquals(second.navigationContext.contextReference, third.navigationContext.contextReference)
    }


}