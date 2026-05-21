@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package dev.enro

import dev.enro.test.NavigationKeyFixtures
import dev.enro.test.fixtures.NavigationContextFixtures
import dev.enro.test.fixtures.NavigationDestinationFixtures
import dev.enro.test.runEnroTest
import dev.enro.ui.EmptyBehavior
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for the [EmptyBehavior] factory builders that consumers use via
 * `rememberNavigationContainer(emptyBehavior = ...)`. The underlying
 * `NavigationContainer.EmptyInterceptor` is already covered by
 * NavigationContainerTests; this file locks down the public wrappers
 * those tests don't exercise.
 *
 * `EmptyBehavior` is also installed on a container by adding its
 * `interceptor` property as an `EmptyInterceptor`, which is what
 * `rememberNavigationContainer` does for us under the hood -- the tests
 * here replicate that wiring directly.
 */
class EmptyBehaviorTests {

    @Test
    fun `preventEmpty denies the container becoming empty`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container

        val destination = NavigationDestinationFixtures.create(NavigationKeyFixtures.SimpleKey())
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, destination)

        val instance = NavigationKeyFixtures.SimpleKey().asInstance()
        container.setBackstackDirect(backstackOf(instance))

        val behavior = EmptyBehavior.preventEmpty()
        container.addEmptyInterceptor(behavior.interceptor)

        container.execute(destinationContext, NavigationOperation.Close(instance))

        assertEquals(
            expected = 1,
            actual = container.backstack.size,
            message = "preventEmpty should block the container from going empty; backstack: ${container.backstack}",
        )
    }

    @Test
    fun `allowEmpty allows the container to become empty and runs the onEmpty callback`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container

        val destination = NavigationDestinationFixtures.create(NavigationKeyFixtures.SimpleKey())
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, destination)

        val instance = NavigationKeyFixtures.SimpleKey().asInstance()
        container.setBackstackDirect(backstackOf(instance))

        var onEmptyInvocations = 0
        val behavior = EmptyBehavior.allowEmpty(onEmpty = { onEmptyInvocations++ })
        container.addEmptyInterceptor(behavior.interceptor)

        container.execute(destinationContext, NavigationOperation.Close(instance))

        assertEquals(
            expected = 0,
            actual = container.backstack.size,
            message = "allowEmpty should permit the container to go empty after the last close",
        )
        assertEquals(
            expected = 1,
            actual = onEmptyInvocations,
            message = "onEmpty callback should fire exactly once when the container empties",
        )
    }

    @Test
    fun `default behavior matches preventEmpty`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container

        val destination = NavigationDestinationFixtures.create(NavigationKeyFixtures.SimpleKey())
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, destination)

        val instance = NavigationKeyFixtures.SimpleKey().asInstance()
        container.setBackstackDirect(backstackOf(instance))

        val behavior = EmptyBehavior.default()
        container.addEmptyInterceptor(behavior.interceptor)

        container.execute(destinationContext, NavigationOperation.Close(instance))

        assertTrue(
            actual = container.backstack.isNotEmpty(),
            message = "default() should behave like preventEmpty(); the close on the last entry must have been denied",
        )
    }
}
