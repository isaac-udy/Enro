@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package dev.enro

import androidx.compose.material3.Text
import dev.enro.controller.createNavigationModule
import dev.enro.result.NavigationResult
import dev.enro.result.NavigationResultChannel
import dev.enro.test.EnroTest
import dev.enro.test.NavigationKeyFixtures
import dev.enro.test.fixtures.NavigationContextFixtures
import dev.enro.test.fixtures.NavigationDestinationFixtures
import dev.enro.test.runEnroTest
import dev.enro.ui.destinations.SyntheticDestination
import dev.enro.ui.destinations.complete
import dev.enro.ui.destinations.completeFrom
import dev.enro.ui.destinations.isSyntheticDestination
import dev.enro.ui.destinations.syntheticDestination
import dev.enro.ui.navigationDestination
import kotlinx.serialization.Serializable
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
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
 *
 * The block can fall through (pure side-effect bridge) or short-circuit by
 * calling one of the outcome methods on the scope (`open`, `close`,
 * `complete`, `completeFrom`). Outcome methods throw a sentinel that the
 * dispatcher catches and converts to a [NavigationOperation].
 */
class SyntheticDestinationTests {

    @AfterTest
    fun clearPendingResults() {
        NavigationResultChannel.pendingResults.value = emptyMap()
    }

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

        val container = openSynthetic(SyntheticTestKey.asInstance())

        assertEquals(
            expected = 1,
            actual = blockExecutions,
            message = "Synthetic block should have been invoked exactly once for the Open",
        )
        assertSame(
            expected = container.destinationContext,
            actual = capturedContext,
            message = "Synthetic block's scope.context should be the originating fromContext",
        )
        assertEquals(
            expected = 0,
            actual = container.container.backstack.size,
            message = "Synthetic destinations must never reach the container backstack",
        )
        assertEquals(
            expected = SyntheticTestKey.asInstance().key,
            actual = capturedInstance?.key,
            message = "Synthetic block's scope.instance should carry the original key",
        )
    }

    @Test
    fun `Synthetic falling through with no outcome leaves the backstack untouched and registers no result`() = runEnroTest {
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<SyntheticTestKey>(
                    syntheticDestination<SyntheticTestKey> {
                        // pure side-effect bridge — no outcome method called
                    }
                )
            }
        )
        val resultId = NavigationResultChannel.Id(ownerId = "owner", resultId = "ch")
        val syntheticInstance = SyntheticTestKey.asInstance().apply {
            metadata.set(NavigationResultChannel.ResultIdKey, resultId)
        }

        val container = openSynthetic(syntheticInstance)

        assertEquals(
            expected = 0,
            actual = container.container.backstack.size,
            message = "Backstack must remain empty when synthetic falls through with no outcome",
        )
        assertNull(
            actual = NavigationResultChannel.pendingResults.value[resultId],
            message = "Falling through should not register any result for the synthetic's channel",
        )
    }

    @Test
    fun `Synthetic open(otherKey) opens that key on the originating container`() = runEnroTest {
        val targetKey = NavigationKeyFixtures.SimpleKey()
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<SyntheticTestKey>(
                    syntheticDestination<SyntheticTestKey> { open(targetKey) }
                )
                destination<NavigationKeyFixtures.SimpleKey>(
                    navigationDestination<NavigationKeyFixtures.SimpleKey> { Text("target") }
                )
            }
        )

        val container = openSynthetic(SyntheticTestKey.asInstance())

        val keys = container.container.backstack.map { it.key }
        assertEquals(
            expected = listOf(targetKey),
            actual = keys,
            message = "Synthetic open(target) should have produced an Open(target) on the originating container; got: $keys",
        )
    }

    @Test
    fun `Synthetic close registers a Closed result for the synthetic's instance`() = runEnroTest {
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<SyntheticTestKey>(
                    syntheticDestination<SyntheticTestKey> { close() }
                )
            }
        )
        val resultId = NavigationResultChannel.Id(ownerId = "owner", resultId = "ch")
        val syntheticInstance = SyntheticTestKey.asInstance().apply {
            metadata.set(NavigationResultChannel.ResultIdKey, resultId)
        }

        openSynthetic(syntheticInstance)

        val pending = NavigationResultChannel.pendingResults.value[resultId]
        assertTrue(
            actual = pending is NavigationResult.Closed,
            message = "Synthetic close() should register a Closed result; got: ${pending?.let { it::class.simpleName }}",
        )
    }

    @Test
    fun `Synthetic complete(result) registers a Completed result with that value`() = runEnroTest {
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<ResultBearingSyntheticTestKey>(
                    syntheticDestination<ResultBearingSyntheticTestKey> { complete("from synthetic") }
                )
            }
        )
        val resultId = NavigationResultChannel.Id(ownerId = "owner", resultId = "ch")
        val syntheticInstance = ResultBearingSyntheticTestKey().asInstance().apply {
            metadata.set(NavigationResultChannel.ResultIdKey, resultId)
        }

        openSynthetic(syntheticInstance)

        val pending = NavigationResultChannel.pendingResults.value[resultId]
        assertTrue(
            actual = pending is NavigationResult.Completed<*>,
            message = "Synthetic complete(result) should register a Completed result",
        )
        assertEquals(
            expected = "from synthetic",
            actual = (pending as NavigationResult.Completed<*>).data,
            message = "Completed result's payload should match what the synthetic passed to complete()",
        )
    }

    @Test
    fun `Synthetic completeFrom forwards result-channel routing to the chosen destination`() = runEnroTest {
        val forwarded = ResultBearingSyntheticTestKey()
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<ResultBearingSyntheticTestKey>(
                    navigationDestination<ResultBearingSyntheticTestKey> { Text("forwarded") }
                )
                destination<ForwardingSyntheticKey>(
                    syntheticDestination<ForwardingSyntheticKey> { completeFrom(forwarded) }
                )
            }
        )
        val resultId = NavigationResultChannel.Id(ownerId = "owner", resultId = "ch")
        val syntheticInstance = ForwardingSyntheticKey().asInstance().apply {
            metadata.set(NavigationResultChannel.ResultIdKey, resultId)
        }

        val container = openSynthetic(syntheticInstance)

        // The forwarded key should have landed in the container's backstack…
        val landed = container.container.backstack.singleOrNull()
        assertTrue(
            actual = landed != null && landed.key == forwarded,
            message = "completeFrom should have opened the forwarded key; backstack was: ${container.container.backstack.map { it.key }}",
        )
        // …and the forwarded instance must carry the synthetic's original ResultIdKey, so
        // when *it* completes, the original caller's channel gets the result.
        assertEquals(
            expected = resultId,
            actual = landed?.metadata?.get(NavigationResultChannel.ResultIdKey),
            message = "completeFrom must copy the synthetic's ResultIdKey onto the forwarded instance so result routing reaches the original caller",
        )
    }
}

private data class OpenedSyntheticContainer(
    val container: NavigationContainer,
    val destinationContext: NavigationContext,
)

private fun openSynthetic(
    syntheticInstance: NavigationKey.Instance<NavigationKey>,
): OpenedSyntheticContainer {
    val rootContext = NavigationContextFixtures.createRootContext()
    val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
    val container = containerContext.container
    container.setFilter(acceptAll())
    container.addInterceptor(SyntheticDestination.interceptor)

    val sourceDestination = NavigationDestinationFixtures.create(NavigationKeyFixtures.SimpleKey())
    val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, sourceDestination)

    container.execute(destinationContext, NavigationOperation.Open(syntheticInstance))
    return OpenedSyntheticContainer(container, destinationContext)
}

@Serializable
data object SyntheticTestKey : NavigationKey

@Serializable
data object RegularSyntheticTestKey : NavigationKey

@Serializable
class ResultBearingSyntheticTestKey : NavigationKey.WithResult<String>

@Serializable
class ForwardingSyntheticKey : NavigationKey.WithResult<String>
