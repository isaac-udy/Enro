@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package dev.enro

import dev.enro.path.NavigationPathBinding
import dev.enro.path.createPathBinding
import dev.enro.test.EnroTest
import dev.enro.test.EnroTestAssertionException
import dev.enro.test.NavigationKeyFixtures
import dev.enro.test.assertBackstackContains
import dev.enro.test.assertBackstackDoesNotContain
import dev.enro.test.assertBackstackEmpty
import dev.enro.test.assertBackstackKeys
import dev.enro.test.assertBackstackSize
import dev.enro.test.assertOperationSequence
import dev.enro.test.assertPathDoesNotResolve
import dev.enro.test.assertPathFor
import dev.enro.test.assertPathResolvesTo
import dev.enro.test.createTestNavigationHandle
import dev.enro.test.fixtures.NavigationContainerFixtures
import dev.enro.test.installNavigationModule
import dev.enro.test.installPathBindings
import dev.enro.test.lastOperation
import dev.enro.test.lastOperationOfType
import dev.enro.test.runEnroTest
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Tests for the enro-test ergonomic helpers shipped alongside the synthetic
 * destination testing API. Covers backstack assertions, the install* module
 * shortcuts, path-resolution assertions, and TestNavigationHandle operation
 * history helpers.
 */
class EnroTestHelpersTests {

    // ---- Backstack assertions ----

    @Test
    fun `assertBackstackSize matches an empty backstack`() = runEnroTest {
        val container = NavigationContainerFixtures.create()
        container.container.assertBackstackSize(0)
    }

    @Test
    fun `assertBackstackSize fails with a helpful message`() = runEnroTest {
        val container = NavigationContainerFixtures.create()

        val error = assertFailsWith<EnroTestAssertionException> {
            container.container.assertBackstackSize(3)
        }
        assertTrue(error.message?.contains("Expected backstack to have 3 entries") == true)
    }

    @Test
    fun `assertBackstackKeys matches exact key sequence`() = runEnroTest {
        val key1 = NavigationKeyFixtures.SimpleKey()
        val key2 = NavigationKeyFixtures.SimpleKey()
        val container = NavigationContainerFixtures.create(
            backstack = backstackOf(key1.asInstance(), key2.asInstance()),
        )
        container.container.assertBackstackKeys(key1, key2)
    }

    @Test
    fun `assertBackstackContains finds a matching key by type`() = runEnroTest {
        val key = HelpersResultKey()
        val container = NavigationContainerFixtures.create(
            backstack = backstackOf(NavigationKeyFixtures.SimpleKey().asInstance(), key.asInstance()),
        )
        val found = container.container.assertBackstackContains<HelpersResultKey>()
        assertEquals(key, found.key)
    }

    @Test
    fun `assertBackstackDoesNotContain succeeds when key type is absent`() = runEnroTest {
        val container = NavigationContainerFixtures.create(
            backstack = backstackOf(NavigationKeyFixtures.SimpleKey().asInstance()),
        )
        container.container.assertBackstackDoesNotContain<HelpersResultKey>()
    }

    @Test
    fun `assertBackstackEmpty passes for empty backstack and fails for non-empty`() = runEnroTest {
        val empty = NavigationContainerFixtures.create()
        empty.container.assertBackstackEmpty()

        val nonEmpty = NavigationContainerFixtures.create(
            backstack = backstackOf(NavigationKeyFixtures.SimpleKey().asInstance()),
        )
        assertFailsWith<EnroTestAssertionException> {
            nonEmpty.container.assertBackstackEmpty()
        }
    }

    // ---- installNavigationModule / installPathBindings ----

    @Test
    fun `installPathBindings registers bindings on the test controller`() = runEnroTest {
        val binding = NavigationPathBinding.createPathBinding<String, HelpersPathKey>(
            pattern = "items/{id}",
            propertyOne = HelpersPathKey::id,
            constructor = ::HelpersPathKey,
        )
        installPathBindings(binding)

        EnroTest.getCurrentNavigationController()
            .assertPathResolvesTo<HelpersPathKey>("/items/abc")
    }

    // ---- Path resolution assertions ----

    @Test
    fun `assertPathResolvesTo returns the typed key matching predicate`() = runEnroTest {
        val binding = NavigationPathBinding.createPathBinding<String, HelpersPathKey>(
            pattern = "items/{id}",
            propertyOne = HelpersPathKey::id,
            constructor = ::HelpersPathKey,
        )
        installPathBindings(binding)
        val controller = EnroTest.getCurrentNavigationController()

        val key = controller.assertPathResolvesTo<HelpersPathKey>("/items/xyz") { it.id == "xyz" }

        assertEquals("xyz", key.id)
    }

    @Test
    fun `assertPathDoesNotResolve passes when no binding matches`() = runEnroTest {
        val controller = EnroTest.getCurrentNavigationController()
        controller.assertPathDoesNotResolve("/items/never-resolved")
    }

    @Test
    fun `assertPathFor checks the reverse-direction serialisation`() = runEnroTest {
        installPathBindings(
            NavigationPathBinding.createPathBinding<String, HelpersPathKey>(
                pattern = "items/{id}",
                propertyOne = HelpersPathKey::id,
                constructor = ::HelpersPathKey,
            )
        )
        val controller = EnroTest.getCurrentNavigationController()

        controller.assertPathFor(HelpersPathKey("zzz"), expectedPath = "/items/zzz")
    }

    @Test
    fun `assertPathResolvesTo fails with a helpful message for wrong type`() = runEnroTest {
        installPathBindings(
            NavigationPathBinding.createPathBinding<String, HelpersPathKey>(
                pattern = "items/{id}",
                propertyOne = HelpersPathKey::id,
                constructor = ::HelpersPathKey,
            )
        )
        val controller = EnroTest.getCurrentNavigationController()

        val error = assertFailsWith<EnroTestAssertionException> {
            controller.assertPathResolvesTo<NavigationKeyFixtures.SimpleKey>("/items/abc")
        }
        assertTrue(error.message?.contains("resolved to") == true)
    }

    // ---- Operation history fluency ----

    @Test
    fun `lastOperation returns the most recent operation`() = runEnroTest {
        val handle = createTestNavigationHandle(HelpersResultKey())
        handle.execute(NavigationOperation.Open(NavigationKeyFixtures.SimpleKey().asInstance()))
        handle.execute(NavigationOperation.Close(handle.instance))

        val last = handle.lastOperation()
        assertTrue(last is NavigationOperation.Close<*>)
    }

    @Test
    fun `lastOperationOfType filters by operation subtype`() = runEnroTest {
        val handle = createTestNavigationHandle(HelpersResultKey())
        handle.execute(NavigationOperation.Open(NavigationKeyFixtures.SimpleKey().asInstance()))
        handle.execute(NavigationOperation.Close(handle.instance))

        val lastOpen = handle.lastOperationOfType<NavigationOperation.Open<*>>()
        assertTrue(lastOpen.instance.key is NavigationKeyFixtures.SimpleKey)
    }

    @Test
    fun `assertOperationSequence enforces type ordering`() = runEnroTest {
        val handle = createTestNavigationHandle(HelpersResultKey())
        handle.execute(NavigationOperation.Open(NavigationKeyFixtures.SimpleKey().asInstance()))
        handle.execute(NavigationOperation.Close(handle.instance))

        handle.assertOperationSequence(
            NavigationOperation.Open::class,
            NavigationOperation.Close::class,
        )
    }

    @Test
    fun `assertOperationSequence fails with mismatched sequence`() = runEnroTest {
        val handle = createTestNavigationHandle(HelpersResultKey())
        handle.execute(NavigationOperation.Open(NavigationKeyFixtures.SimpleKey().asInstance()))

        val error = assertFailsWith<EnroTestAssertionException> {
            handle.assertOperationSequence(
                NavigationOperation.Close::class,
                NavigationOperation.Open::class,
            )
        }
        assertTrue(error.message?.contains("Expected operation sequence") == true)
    }

    @Test
    fun `lastOperation fails when no operations were executed`() = runEnroTest {
        val handle = createTestNavigationHandle(HelpersResultKey())

        assertFailsWith<EnroTestAssertionException> {
            handle.lastOperation()
        }
    }
}

@Serializable
data class HelpersPathKey(val id: String) : NavigationKey

@Serializable
class HelpersResultKey : NavigationKey.WithResult<String>
