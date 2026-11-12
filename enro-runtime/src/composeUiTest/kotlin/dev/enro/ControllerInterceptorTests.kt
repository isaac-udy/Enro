package dev.enro

import androidx.compose.material3.Text
import dev.enro.controller.createNavigationModule
import dev.enro.controller.defaultNavigationModule
import dev.enro.controller.interceptors.PreviouslyActiveContainerInterceptor
import dev.enro.controller.interceptors.RootDestinationInterceptor
import dev.enro.test.EnroTest
import dev.enro.test.NavigationKeyFixtures
import dev.enro.test.fixtures.NavigationContextFixtures
import dev.enro.test.runEnroTest
import dev.enro.ui.destinations.rootContextDestination
import dev.enro.ui.navigationDestination
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests [RootDestinationInterceptor] and [PreviouslyActiveContainerInterceptor], which
 * [defaultNavigationModule] installs. The tests call the interceptor functions directly:
 * the result of each function depends only on `fromContext`, `containerContext` and the
 * operations.
 */
class ControllerInterceptorTests {

    @Test
    fun `defaultNavigationModule registers both built-in controller interceptors`() {
        val interceptors = defaultNavigationModule.interceptors
        assertTrue(
            actual = interceptors.contains(RootDestinationInterceptor),
            message = "defaultNavigationModule should install RootDestinationInterceptor",
        )
        assertTrue(
            actual = interceptors.contains(PreviouslyActiveContainerInterceptor),
            message = "defaultNavigationModule should install PreviouslyActiveContainerInterceptor",
        )
    }

    @Test
    fun `PreviouslyActiveContainerInterceptor records cross-container active state on Open and adds reactivation SideEffect on Close`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerA = NavigationContextFixtures.createContainerContext(rootContext)
        val containerB = NavigationContextFixtures.createContainerContext(rootContext)
        rootContext.registerChild(containerA)
        rootContext.registerChild(containerB)
        // registerVisibility makes containerB the active child, because containerA is not visible.
        rootContext.registerVisibility(containerB, isVisible = true)

        val instance = NavigationKeyFixtures.SimpleKey().asInstance()
        val openOp = NavigationOperation.Open(instance)

        // The Open targets containerA while containerB is active, so the interceptor stores the
        // id of containerB in the instance metadata.
        PreviouslyActiveContainerInterceptor.intercept(
            fromContext = rootContext,
            containerContext = containerA,
            operation = openOp,
        )

        val closeOp = NavigationOperation.Close(instance)
        val processed = PreviouslyActiveContainerInterceptor.beforeIntercept(
            fromContext = rootContext,
            containerContext = containerA,
            operations = listOf(closeOp),
        )

        assertEquals(
            expected = 2,
            actual = processed.size,
            message = "Close should be augmented with a SideEffect when a previously-active container was recorded",
        )
        assertEquals(closeOp, processed[0], "Original Close op should be first")
        assertTrue(
            actual = processed[1] is NavigationOperation.SideEffect,
            message = "Second op should be the reactivation SideEffect; was: ${processed[1]::class.simpleName}",
        )
    }

    @Test
    fun `PreviouslyActiveContainerInterceptor does not add SideEffect when no previous container was recorded`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerA = NavigationContextFixtures.createContainerContext(rootContext)
        rootContext.registerChild(containerA)
        rootContext.registerVisibility(containerA, isVisible = true)

        val instance = NavigationKeyFixtures.SimpleKey().asInstance()
        val openOp = NavigationOperation.Open(instance)

        // The Open targets the active container, so the interceptor stores no metadata.
        PreviouslyActiveContainerInterceptor.intercept(
            fromContext = rootContext,
            containerContext = containerA,
            operation = openOp,
        )

        val closeOp = NavigationOperation.Close(instance)
        val processed = PreviouslyActiveContainerInterceptor.beforeIntercept(
            fromContext = rootContext,
            containerContext = containerA,
            operations = listOf(closeOp),
        )

        assertEquals(
            expected = listOf(closeOp),
            actual = processed,
            message = "No SideEffect should be appended when there's no recorded previous container",
        )
    }

    @Test
    fun `RootDestinationInterceptor extracts root-context Opens into a single SideEffect`() = runEnroTest {
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<RegularControllerInterceptorKey>(
                    navigationDestination<RegularControllerInterceptorKey> { Text("regular") }
                )
                destination<RootControllerInterceptorKey>(
                    navigationDestination<RootControllerInterceptorKey>(
                        metadata = { rootContextDestination() },
                    ) { Text("root") }
                )
            }
        )

        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        rootContext.registerChild(containerContext)

        val regularOp = NavigationOperation.Open(RegularControllerInterceptorKey.asInstance())
        val rootOp = NavigationOperation.Open(RootControllerInterceptorKey.asInstance())

        val processed = RootDestinationInterceptor.beforeIntercept(
            fromContext = rootContext,
            containerContext = containerContext,
            operations = listOf(regularOp, rootOp),
        )

        assertEquals(
            expected = 2,
            actual = processed.size,
            message = "Regular op should remain, root op should be replaced by one SideEffect; processed: $processed",
        )
        assertEquals(
            expected = regularOp,
            actual = processed[0],
            message = "Regular op should be left untouched",
        )
        assertTrue(
            actual = processed[1] is NavigationOperation.SideEffect,
            message = "Root op should have been redirected into a SideEffect; was: ${processed[1]::class.simpleName}",
        )
    }

    @Test
    fun `RootDestinationInterceptor passes operations through unchanged when none target root-context destinations`() = runEnroTest {
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<RegularControllerInterceptorKey>(
                    navigationDestination<RegularControllerInterceptorKey> { Text("regular") }
                )
            }
        )

        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        rootContext.registerChild(containerContext)

        val operations = listOf(
            NavigationOperation.Open(RegularControllerInterceptorKey.asInstance()),
            NavigationOperation.Open(RegularControllerInterceptorKey.asInstance()),
        )

        val processed = RootDestinationInterceptor.beforeIntercept(
            fromContext = rootContext,
            containerContext = containerContext,
            operations = operations,
        )

        assertEquals(
            expected = operations,
            actual = processed,
            message = "Operations should pass through unchanged when no root-context destinations are present",
        )
    }
}

@Serializable
data object RegularControllerInterceptorKey : NavigationKey

@Serializable
data object RootControllerInterceptorKey : NavigationKey
