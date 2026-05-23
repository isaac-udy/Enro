@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package dev.enro

import androidx.compose.material3.Text
import dev.enro.controller.createNavigationModule
import dev.enro.test.EnroTest
import dev.enro.test.NavigationKeyFixtures
import dev.enro.test.fixtures.NavigationContextFixtures
import dev.enro.test.fixtures.NavigationDestinationFixtures
import dev.enro.test.runEnroTest
import dev.enro.ui.destinations.SyntheticDestination
import dev.enro.ui.destinations.isSyntheticDestination
import dev.enro.ui.destinations.syntheticDestination
import dev.enro.ui.navigationDestination
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Coverage for [SyntheticDestination] and its registered interceptor.
 * The synthetic destination mechanism is the third built-in
 * controller interceptor (alongside RootDestinationInterceptor and
 * PreviouslyActiveContainerInterceptor) and had zero unit coverage.
 *
 * A synthetic destination is "a NavigationKey that, when opened, runs a
 * block of code instead of rendering anything" -- the interceptor
 * recognises the open, replaces it with a SideEffect that invokes the
 * synthetic block, and the destination never reaches the backstack.
 * It's the redirect / fire-and-forget primitive in Enro.
 */
class SyntheticDestinationTests {

    @Test
    fun `isSyntheticDestination returns true for instances bound to a synthetic destination`() = runEnroTest {
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<SyntheticTestKey>(
                    syntheticDestination<SyntheticTestKey> { /* no-op */ }
                )
            }
        )
        val instance = SyntheticTestKey.asInstance()

        assertTrue(
            actual = isSyntheticDestination(instance),
            message = "Instance bound to a syntheticDestination provider should be reported as synthetic",
        )
    }

    @Test
    fun `isSyntheticDestination returns false for regular destinations`() = runEnroTest {
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<RegularSyntheticTestKey>(
                    navigationDestination<RegularSyntheticTestKey> { Text("regular") }
                )
            }
        )
        val instance = RegularSyntheticTestKey.asInstance()

        assertFalse(
            actual = isSyntheticDestination(instance),
            message = "Regular destinations must not be detected as synthetic",
        )
    }

    @Test
    fun `Opening a synthetic destination runs its block with the right scope and does not add to the backstack`() = runEnroTest {
        // In production, the SyntheticDestination.interceptor is installed
        // via defaultNavigationModule on EnroController. commonTest uses a
        // bare controller, so we attach the same interceptor manually to
        // exercise the full path: Open(synthetic) -> interceptor returns
        // SideEffect -> SideEffect calls executeSynthetic -> the
        // syntheticDestination block runs with a scope carrying the
        // originating fromContext and the original instance.
        var blockExecutions = 0
        var capturedContext: NavigationContext? = null
        var capturedInstance: NavigationKey.Instance<*>? = null

        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<SyntheticTestKey>(
                    syntheticDestination<SyntheticTestKey> {
                        blockExecutions++
                        capturedContext = context
                        capturedInstance = instance
                    }
                )
            }
        )

        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container
        container.setFilter(acceptAll())
        container.addInterceptor(SyntheticDestination.interceptor)

        val sourceDestination = NavigationDestinationFixtures.create(NavigationKeyFixtures.SimpleKey())
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, sourceDestination)

        val syntheticInstance = SyntheticTestKey.asInstance()
        container.execute(destinationContext, NavigationOperation.Open(syntheticInstance))

        assertEquals(
            expected = 1,
            actual = blockExecutions,
            message = "Synthetic block should have been invoked exactly once for the Open",
        )
        assertSame(
            expected = destinationContext,
            actual = capturedContext,
            message = "Synthetic block's scope.context should be the originating fromContext",
        )
        assertSame(
            expected = syntheticInstance,
            actual = capturedInstance,
            message = "Synthetic block's scope.instance should be the original instance from the Open",
        )
        assertEquals(
            expected = 0,
            actual = container.backstack.size,
            message = "Synthetic destinations must never reach the container backstack",
        )
    }
}

@Serializable
data object SyntheticTestKey : NavigationKey

@Serializable
data object RegularSyntheticTestKey : NavigationKey
