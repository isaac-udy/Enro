@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package dev.enro

import androidx.compose.material3.Text
import dev.enro.controller.createNavigationModule
import dev.enro.test.EnroTest
import dev.enro.test.assertCloses
import dev.enro.test.assertCompletes
import dev.enro.test.assertCompletesFrom
import dev.enro.test.assertOpens
import dev.enro.test.assertSideEffect
import dev.enro.test.runEnroTest
import dev.enro.test.runWith
import dev.enro.test.testSyntheticDestination
import dev.enro.ui.destinations.complete
import dev.enro.ui.destinations.completeFrom
import dev.enro.ui.destinations.syntheticDestination
import dev.enro.ui.navigationDestination
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Tests for the `testSyntheticDestination` helpers in enro-test. Covers each
 * outcome path through both entry points: looking up the synthetic via the
 * controller's bindings (the "registered" path), and providing the
 * NavigationDestinationProvider directly (the "direct" path that doesn't
 * need a controller install).
 */
class SyntheticDestinationTesterTests {

    // ---- Registered-path tests ----

    @Test
    fun `testSyntheticDestination by key finds the synthetic via controller bindings`() = runEnroTest {
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<SyntheticTesterOpenKey>(
                    syntheticDestination<SyntheticTesterOpenKey> { open(SyntheticTesterTargetKey()) }
                )
            }
        )

        val outcome = testSyntheticDestination(SyntheticTesterOpenKey)

        outcome.assertOpens<SyntheticTesterTargetKey>()
    }

    @Test
    fun `Registered close outcome is reported as Close`() = runEnroTest {
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<SyntheticTesterOpenKey>(
                    syntheticDestination<SyntheticTesterOpenKey> { close() }
                )
            }
        )

        testSyntheticDestination(SyntheticTesterOpenKey).assertCloses(silent = false)
    }

    @Test
    fun `Registered closeSilently outcome is reported as Close with silent=true`() = runEnroTest {
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<SyntheticTesterOpenKey>(
                    syntheticDestination<SyntheticTesterOpenKey> { closeSilently() }
                )
            }
        )

        testSyntheticDestination(SyntheticTesterOpenKey).assertCloses(silent = true)
    }

    @Test
    fun `Registered complete with result is reported as Complete with the payload`() = runEnroTest {
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<SyntheticTesterResultKey>(
                    syntheticDestination<SyntheticTesterResultKey> { complete("hello") }
                )
            }
        )

        testSyntheticDestination(SyntheticTesterResultKey()).assertCompletes(expectedResult = "hello")
    }

    @Test
    fun `Registered completeFrom is reported as CompleteFrom of the forwarded key`() = runEnroTest {
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<SyntheticTesterResultKey>(
                    navigationDestination<SyntheticTesterResultKey> { Text("forwarded") }
                )
                destination<SyntheticTesterForwarderKey>(
                    syntheticDestination<SyntheticTesterForwarderKey> { completeFrom(SyntheticTesterResultKey()) }
                )
            }
        )

        testSyntheticDestination(SyntheticTesterForwarderKey())
            .assertCompletesFrom<SyntheticTesterResultKey>()
    }

    @Test
    fun `Registered fall-through is reported as a silent close`() = runEnroTest {
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<SyntheticTesterOpenKey>(
                    syntheticDestination<SyntheticTesterOpenKey> {
                        // no outcome method
                    }
                )
            }
        )

        testSyntheticDestination(SyntheticTesterOpenKey).assertCloses(silent = true)
    }

    @Test
    fun `testSyntheticDestination by key throws a clear error when key isn't bound`() = runEnroTest {
        EnroTest.getCurrentNavigationController().addModule(createNavigationModule {})

        val error = assertFailsWith<IllegalStateException> {
            testSyntheticDestination(SyntheticTesterOpenKey)
        }
        assertTrue(
            actual = error.message?.contains("not bound to a synthetic destination") == true,
            message = "Error should explain the synthetic isn't bound; was: ${error.message}",
        )
    }

    @Test
    fun `testSyntheticDestination by key throws when the bound destination is not a synthetic`() = runEnroTest {
        EnroTest.getCurrentNavigationController().addModule(
            createNavigationModule {
                destination<SyntheticTesterOpenKey>(
                    navigationDestination<SyntheticTesterOpenKey> { Text("ordinary destination") }
                )
            }
        )

        val error = assertFailsWith<IllegalStateException> {
            testSyntheticDestination(SyntheticTesterOpenKey)
        }
        assertTrue(
            actual = error.message?.contains("not bound to a synthetic destination") == true,
            message = "Error should explain that the binding isn't a synthetic; was: ${error.message}",
        )
    }

    // ---- Direct-path (no controller install) tests ----

    @Test
    fun `testSyntheticDestination by key and provider works without installing the synthetic on the controller`() = runEnroTest {
        // No addModule call — the synthetic provider is passed directly.
        val provider = syntheticDestination<SyntheticTesterOpenKey> { open(SyntheticTesterTargetKey()) }

        val outcome = testSyntheticDestination(SyntheticTesterOpenKey, provider)

        outcome.assertOpens<SyntheticTesterTargetKey>()
    }

    @Test
    fun `Direct-path supports each outcome shape`() = runEnroTest {
        val openProvider = syntheticDestination<SyntheticTesterOpenKey> { open(SyntheticTesterTargetKey()) }
        val closeProvider = syntheticDestination<SyntheticTesterOpenKey> { close() }
        val completeProvider = syntheticDestination<SyntheticTesterResultKey> { complete("ok") }
        val sideEffectProvider = syntheticDestination<SyntheticTesterOpenKey> {
            sideEffect { /* no-op */ }
        }

        testSyntheticDestination(SyntheticTesterOpenKey, openProvider).assertOpens<SyntheticTesterTargetKey>()
        testSyntheticDestination(SyntheticTesterOpenKey, closeProvider).assertCloses(silent = false)
        testSyntheticDestination(SyntheticTesterResultKey(), completeProvider).assertCompletes("ok")
        testSyntheticDestination(SyntheticTesterOpenKey, sideEffectProvider).assertSideEffect()
    }

    @Test
    fun `Direct-path throws when the provider isn't a synthetic`() = runEnroTest {
        val notSynthetic = navigationDestination<SyntheticTesterOpenKey> { Text("ordinary") }

        val error = assertFailsWith<IllegalStateException> {
            testSyntheticDestination(SyntheticTesterOpenKey, notSynthetic)
        }
        assertTrue(
            actual = error.message?.contains("not a synthetic destination") == true,
            message = "Error should explain the provider isn't synthetic; was: ${error.message}",
        )
    }

    // ---- Side-effect tests ----

    @Test
    fun `SideEffect runWith default fixtures executes the block`() = runEnroTest {
        var ran = false
        val provider = syntheticDestination<SyntheticTesterOpenKey> {
            sideEffect { ran = true }
        }

        val outcome = testSyntheticDestination(SyntheticTesterOpenKey, provider)

        outcome.assertSideEffect().runWith()
        assertTrue(ran, "Side-effect block should have run")
    }

    @Test
    fun `SideEffect block sees the instance and key from the synthetic`() = runEnroTest {
        var capturedKey: NavigationKey? = null
        val provider = syntheticDestination<SyntheticTesterOpenKey> {
            sideEffect { capturedKey = key }
        }

        testSyntheticDestination(SyntheticTesterOpenKey, provider)
            .assertSideEffect()
            .runWith()

        assertEquals(SyntheticTesterOpenKey, capturedKey)
    }
}

@Serializable
data object SyntheticTesterOpenKey : NavigationKey

@Serializable
data class SyntheticTesterTargetKey(val id: String = "target") : NavigationKey

@Serializable
class SyntheticTesterResultKey : NavigationKey.WithResult<String>

@Serializable
class SyntheticTesterForwarderKey : NavigationKey.WithResult<String>
