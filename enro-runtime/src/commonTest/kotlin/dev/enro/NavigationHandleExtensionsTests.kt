@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package dev.enro

import dev.enro.test.NavigationKeyFixtures
import dev.enro.test.createTestNavigationHandle
import dev.enro.test.runEnroTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for the smaller NavigationHandle extension functions that don't
 * already have dedicated coverage. Today this file just exercises
 * `closeAndReplaceWith`; new extension-level tests can land here as
 * they're written.
 */
class NavigationHandleExtensionsTests {

    @Test
    fun `closeAndReplaceWith dispatches Close of self and Open of the replacement key`() = runEnroTest {
        val handle = createTestNavigationHandle(NavigationKeyFixtures.SimpleKey())
        val replacementKey = NavigationKeyFixtures.SimpleKey()

        handle.closeAndReplaceWith(replacementKey)

        assertEquals(
            expected = 2,
            actual = handle.operations.size,
            message = "closeAndReplaceWith should unpack to Close(self) + Open(replacement); operations: ${handle.operations}",
        )

        val firstOp = handle.operations[0]
        assertTrue(firstOp is NavigationOperation.Close<*>, "First op must be Close")
        assertEquals(
            expected = handle.instance.id,
            actual = (firstOp as NavigationOperation.Close<*>).instance.id,
            message = "Close must target the handle's own instance",
        )

        val secondOp = handle.operations[1]
        assertTrue(secondOp is NavigationOperation.Open<*>, "Second op must be Open")
        assertEquals(
            expected = replacementKey,
            actual = (secondOp as NavigationOperation.Open<*>).instance.key,
            message = "Open's instance must carry the replacement key",
        )
    }
}
