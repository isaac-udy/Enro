package dev.enro

import dev.enro.test.NavigationKeyFixtures
import dev.enro.test.assertClosed
import dev.enro.test.assertNotClosed
import dev.enro.test.createTestNavigationHandle
import dev.enro.test.runEnroTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests that lock down the semantics of [NavigationHandle.requestClose] /
 * [NavigationHandle.close] and the [NavigationHandleConfiguration.onCloseRequested]
 * callback registry. These two entry points behave differently — `close()`
 * unconditionally executes a Close; `requestClose()` consults any registered
 * `onCloseRequested` callback first — and that distinction is currently held
 * in place only by code review.
 */
class NavigationHandleConfigurationTests {

    @Test
    fun `requestClose with a registered callback runs the callback and does NOT execute Close`() = runEnroTest {
        val handle = createTestNavigationHandle(NavigationKeyFixtures.SimpleKey())
        var callbackRan = false
        NavigationHandleConfiguration(handle).onCloseRequested {
            callbackRan = true
        }

        handle.requestClose()

        assertTrue(callbackRan, "onCloseRequested callback should fire")
        handle.assertNotClosed()
    }

    @Test
    fun `requestClose with no callback executes Close directly`() = runEnroTest {
        val handle = createTestNavigationHandle(NavigationKeyFixtures.SimpleKey())

        handle.requestClose()

        handle.assertClosed()
    }

    @Test
    fun `close bypasses onCloseRequested callback`() = runEnroTest {
        val handle = createTestNavigationHandle(NavigationKeyFixtures.SimpleKey())
        var callbackRan = false
        NavigationHandleConfiguration(handle).onCloseRequested {
            callbackRan = true
        }

        handle.close()

        assertFalse(callbackRan, "close() should not consult onCloseRequested callbacks")
        handle.assertClosed()
    }

    @Test
    fun `Multiple onCloseRequested callbacks throw with a clear error`() = runEnroTest {
        val handle = createTestNavigationHandle(NavigationKeyFixtures.SimpleKey())
        NavigationHandleConfiguration(handle).onCloseRequested { /* first */ }
        NavigationHandleConfiguration(handle).onCloseRequested { /* second */ }

        val error = assertFailsWith<IllegalStateException> {
            handle.requestClose()
        }
        assertTrue(
            actual = error.message?.contains("Multiple onCloseRequested callbacks") == true,
            message = "Error should call out duplicate onCloseRequested registration; was: ${error.message}",
        )
    }
}
