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
import dev.enro.ui.destinations.isSyntheticDestination
import dev.enro.ui.destinations.syntheticDestination
import dev.enro.ui.navigationDestination
import kotlinx.serialization.Serializable
import kotlin.test.*

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
    fun `Synthetic open otherKey opens that key on the originating container`() = runEnroTest {
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
    fun `Synthetic complete with result registers a Completed result with that value`() = runEnroTest {
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
    fun `Synthetic closeSilently does not register a result for the synthetic's channel`() = runEnroTest {
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<SyntheticTestKey>(
                    syntheticDestination<SyntheticTestKey> { closeSilently() }
                )
            }
        )
        val resultId = NavigationResultChannel.Id(ownerId = "owner", resultId = "ch")
        val syntheticInstance = SyntheticTestKey.asInstance().apply {
            metadata.set(NavigationResultChannel.ResultIdKey, resultId)
        }

        openSynthetic(syntheticInstance)

        assertNull(
            actual = NavigationResultChannel.pendingResults.value[resultId],
            message = "closeSilently() must not publish a result; the original caller's onClosed should not fire",
        )
    }

    @Test
    fun `Synthetic close does not strip the parent destination from the backstack`() = runEnroTest {
        // Belt-and-braces test: the synthetic's Close operation targets the
        // synthetic's instance (which is never in any backstack), so the
        // parent destination — the one that opened the synthetic — must
        // remain on the backstack untouched.
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<SyntheticTestKey>(
                    syntheticDestination<SyntheticTestKey> { close() }
                )
                destination<NavigationKeyFixtures.SimpleKey>(
                    navigationDestination<NavigationKeyFixtures.SimpleKey> { Text("parent") }
                )
            }
        )

        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container
        container.setFilter(acceptAll())
        container.addInterceptor(SyntheticDestination.interceptor)

        val parentKey = NavigationKeyFixtures.SimpleKey()
        val parentInstance = parentKey.asInstance()
        container.setBackstackDirect(backstackOf(parentInstance))

        val parentDestination = NavigationDestinationFixtures.create(parentKey)
        val parentContext = NavigationContextFixtures.createDestinationContext(containerContext, parentDestination)

        container.execute(parentContext, NavigationOperation.Open(SyntheticTestKey.asInstance()))

        assertEquals(
            expected = listOf(parentInstance.id),
            actual = container.backstack.map { it.id },
            message = "Synthetic close must not affect the backstack of whichever destination opened it",
        )
    }

    @Test
    fun `Calling an outcome method after the synthetic finished throws already-finished`() = runEnroTest {
        // Simulates the "block launched a coroutine that outlived the
        // block" case. We can't easily await a real coroutine in a test,
        // so we capture the scope and invoke an outcome method after the
        // dispatcher has moved on — same shape, same failure mode.
        var capturedScope: dev.enro.ui.destinations.SyntheticDestinationScope<SyntheticTestKey>? = null
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<SyntheticTestKey>(
                    syntheticDestination<SyntheticTestKey> {
                        capturedScope = this
                        // Falling through — dispatcher will mark this as silent close.
                    }
                )
            }
        )

        openSynthetic(SyntheticTestKey.asInstance())

        val scope = requireNotNull(capturedScope) { "scope should have been captured by the block" }
        val error = kotlin.runCatching { scope.close() }.exceptionOrNull()
        assertTrue(
            actual = error is IllegalStateException,
            message = "Late outcome call after fall-through should throw IllegalStateException; was: ${error?.let { it::class.simpleName }}",
        )
        assertTrue(
            actual = error?.message?.contains("already finished") == true,
            message = "Error message should mention the synthetic already finished; was: ${error?.message}",
        )
    }

    @Test
    fun `Pure synthetic outcomes are rewritten in place and preserve initial-backstack ordering`() = runEnroTest {
        // A synthetic that opens TargetKey, placed as the FIRST entry in an
        // AggregateOperation alongside RegularKey, should land as
        // [TargetKey, RegularKey]. The synthetic's outcome must replace
        // the synthetic's Open in the same processing pass — not be
        // appended at the end via a separate execute call.
        val targetKey = NavigationKeyFixtures.SimpleKey()
        val regularKey = NavigationKeyFixtures.SimpleKey()
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<SyntheticTestKey>(
                    syntheticDestination<SyntheticTestKey> { open(targetKey) }
                )
                destination<NavigationKeyFixtures.SimpleKey>(
                    navigationDestination<NavigationKeyFixtures.SimpleKey> { Text("regular") }
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

        container.execute(
            destinationContext,
            NavigationOperation.AggregateOperation(
                NavigationOperation.Open(SyntheticTestKey.asInstance()),
                NavigationOperation.Open(regularKey.asInstance()),
            ),
        )

        assertEquals(
            expected = listOf(targetKey, regularKey),
            actual = container.backstack.map { it.key },
            message = "Pure synthetic outcomes must be rewritten in place — backstack ordering was: ${container.backstack.map { it.key }}",
        )
    }

    @Test
    fun `Side-effect synthetic runs after the surrounding pass settles`() = runEnroTest {
        // Assert that when the side-effect block runs, the container's
        // backstack already reflects the other operations from the same
        // processing pass — i.e. the side effect is deferred to
        // afterExecution as advertised.
        var observedBackstackAtSideEffect: List<NavigationKey>? = null
        val regularKey = NavigationKeyFixtures.SimpleKey()
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<SyntheticTestKey>(
                    syntheticDestination<SyntheticTestKey> {
                        sideEffect {
                            observedBackstackAtSideEffect = container.backstack.map { it.key }
                        }
                    }
                )
                destination<NavigationKeyFixtures.SimpleKey>(
                    navigationDestination<NavigationKeyFixtures.SimpleKey> { Text("regular") }
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

        container.execute(
            destinationContext,
            NavigationOperation.AggregateOperation(
                NavigationOperation.Open(SyntheticTestKey.asInstance()),
                NavigationOperation.Open(regularKey.asInstance()),
            ),
        )

        assertEquals(
            expected = listOf<NavigationKey>(regularKey),
            actual = observedBackstackAtSideEffect,
            message = "Side-effect block should run after the surrounding pass settled; observed: $observedBackstackAtSideEffect",
        )
    }

    @Test
    fun `Side-effect synthetic can rewrite the backstack via container execute`() = runEnroTest {
        // Use sideEffect's container reference to perform a SetBackstack —
        // the deferred-execution model that the framework prescribes for
        // anything that can't be expressed as a pure outcome.
        val newRootKey = NavigationKeyFixtures.SimpleKey()
        val newTopKey = NavigationKeyFixtures.SimpleKey()
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<SyntheticTestKey>(
                    syntheticDestination<SyntheticTestKey> {
                        sideEffect {
                            container.execute(
                                context,
                                NavigationOperation.SetBackstack(
                                    currentBackstack = container.backstack,
                                    targetBackstack = backstackOf(
                                        newRootKey.asInstance(),
                                        newTopKey.asInstance(),
                                    ),
                                ),
                            )
                        }
                    }
                )
                destination<NavigationKeyFixtures.SimpleKey>(
                    navigationDestination<NavigationKeyFixtures.SimpleKey> { Text("any") }
                )
            }
        )

        val container = openSynthetic(SyntheticTestKey.asInstance()).container

        assertEquals(
            expected = listOf(newRootKey, newTopKey),
            actual = container.backstack.map { it.key },
            message = "Side-effect's SetBackstack should have rewritten the backstack; was: ${container.backstack.map { it.key }}",
        )
    }

    @Test
    fun `Self-referential synthetic outcome trips the recursion guard`() = runEnroTest {
        // A synthetic whose pure outcome opens itself would loop forever
        // inside processOperations without a guard. We expect a clear
        // IllegalStateException naming the offender.
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<SyntheticTestKey>(
                    syntheticDestination<SyntheticTestKey> { open(SyntheticTestKey) }
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

        val error = kotlin.runCatching {
            container.execute(destinationContext, NavigationOperation.Open(SyntheticTestKey.asInstance()))
        }.exceptionOrNull()

        assertTrue(
            actual = error is IllegalStateException,
            message = "Expected IllegalStateException from the recursion guard; got: ${error?.let { it::class.simpleName }}",
        )
        assertTrue(
            actual = error?.message?.contains("exceeded") == true,
            message = "Recursion guard error should mention exceeding the iteration limit; was: ${error?.message}",
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
