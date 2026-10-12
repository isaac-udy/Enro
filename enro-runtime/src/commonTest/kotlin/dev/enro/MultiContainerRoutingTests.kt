package dev.enro

import dev.enro.handle.findContainerForOperation
import dev.enro.test.NavigationKeyFixtures
import dev.enro.test.fixtures.NavigationContextFixtures
import dev.enro.test.fixtures.NavigationDestinationFixtures
import dev.enro.test.runEnroTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Tests that lock down the routing decision made by
 * [findContainerForOperation] when more than one container is reachable
 * from the originating navigation context. Until now, only
 * [NavigationContainer.accepts] has been tested in isolation — the actual
 * "given this context and this operation, which container handles it"
 * resolution was untested.
 */
class MultiContainerRoutingTests {

    @Test
    fun `findContainerForOperation routes Open to the accepting sibling container`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerA = NavigationContextFixtures.createContainerContext(rootContext)
        val containerB = NavigationContextFixtures.createContainerContext(rootContext)
        rootContext.registerChild(containerA)
        rootContext.registerChild(containerB)

        val destinationInA = NavigationContextFixtures.createDestinationContext(
            containerA,
            NavigationDestinationFixtures.create(NavigationKeyFixtures.SimpleKey()),
        )

        val keyForA = NavigationKeyFixtures.SimpleKey()
        val keyForB = NavigationKeyFixtures.SimpleKey()
        containerA.container.setFilter(
            NavigationContainerFilter(fromChildrenOnly = false) { it.key == keyForA }
        )
        containerB.container.setFilter(
            NavigationContainerFilter(fromChildrenOnly = false) { it.key == keyForB }
        )

        val routedForB = findContainerForOperation(
            fromContext = destinationInA,
            operation = NavigationOperation.Open(keyForB.asInstance()),
        )
        assertSame(
            expected = containerB,
            actual = routedForB,
            message = "Open for keyForB should route to containerB even though the source is a child of containerA",
        )

        val routedForA = findContainerForOperation(
            fromContext = destinationInA,
            operation = NavigationOperation.Open(keyForA.asInstance()),
        )
        assertSame(
            expected = containerA,
            actual = routedForA,
            message = "Open for keyForA should route to containerA",
        )
    }

    @Test
    fun `findContainerForOperation returns null when no container accepts the operation`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerA = NavigationContextFixtures.createContainerContext(rootContext)
        rootContext.registerChild(containerA)

        val destinationInA = NavigationContextFixtures.createDestinationContext(
            containerA,
            NavigationDestinationFixtures.create(NavigationKeyFixtures.SimpleKey()),
        )
        containerA.container.setFilter(acceptNone())

        val routed = findContainerForOperation(
            fromContext = destinationInA,
            operation = NavigationOperation.Open(NavigationKeyFixtures.SimpleKey().asInstance()),
        )
        assertNull(
            actual = routed,
            message = "When no reachable container accepts the Open, findContainerForOperation should return null",
        )
    }

    @Test
    fun `findContainerForOperation does not route to a sibling whose filter is fromChildrenOnly`() = runEnroTest {
        // Both containers accept the same predicate, but containerB additionally
        // gates on fromChildrenOnly. The source destination is under containerA,
        // so containerB's filter should reject the routing attempt and the
        // resolver should fall through to containerA.
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerA = NavigationContextFixtures.createContainerContext(rootContext)
        val containerB = NavigationContextFixtures.createContainerContext(rootContext)
        rootContext.registerChild(containerA)
        rootContext.registerChild(containerB)

        val destinationInA = NavigationContextFixtures.createDestinationContext(
            containerA,
            NavigationDestinationFixtures.create(NavigationKeyFixtures.SimpleKey()),
        )

        containerA.container.setFilter(NavigationContainerFilter(fromChildrenOnly = false) { true })
        containerB.container.setFilter(NavigationContainerFilter(fromChildrenOnly = true) { true })

        val routed = findContainerForOperation(
            fromContext = destinationInA,
            operation = NavigationOperation.Open(NavigationKeyFixtures.SimpleKey().asInstance()),
        )

        assertSame(
            expected = containerA,
            actual = routed,
            message = "containerB has fromChildrenOnly = true and the source is not a child of it — should fall through to containerA",
        )
    }

    @Test
    fun `findContainerForOperation skips the containers of a destination that has left its backstack`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val parentContainer = NavigationContextFixtures.createContainerContext(rootContext)
        rootContext.registerChild(parentContainer)

        val destinationA = NavigationDestinationFixtures.create<NavigationKey>(NavigationKeyFixtures.SimpleKey())
        val destinationB = NavigationDestinationFixtures.create<NavigationKey>(NavigationKeyFixtures.SimpleKey())
        val contextA = NavigationContextFixtures.createDestinationContext(parentContainer, destinationA)
        val contextB = NavigationContextFixtures.createDestinationContext(parentContainer, destinationB)
        parentContainer.registerChild(contextA)
        parentContainer.registerChild(contextB)
        val containerInB = NavigationContextFixtures.createContainerContext(contextB)
        contextB.registerChild(containerInB)

        parentContainer.container.setFilter(acceptAll())
        containerInB.container.setFilter(acceptAll())
        parentContainer.container.setBackstackDirect(backstackOf(destinationA.instance, destinationB.instance))

        val open = NavigationOperation.Open(NavigationKeyFixtures.SimpleKey().asInstance())
        assertSame(
            expected = containerInB,
            actual = findContainerForOperation(fromContext = parentContainer, operation = open),
            message = "While B is in the backstack, its accepting container receives the Open",
        )

        parentContainer.container.setBackstackDirect(backstackOf(destinationA.instance))
        assertSame(
            expected = parentContainer,
            actual = findContainerForOperation(fromContext = parentContainer, operation = open),
            message = "Once B has left the backstack, its container must not receive the Open, even while B is still registered",
        )
    }

    @Test
    fun `fromChildrenOnly keeps the wrapped filter's acceptance`() {
        val key = NavigationKeyFixtures.SimpleKey()
        val filter = accept { key(key) }.fromChildrenOnly()

        assertTrue(filter.fromChildrenOnly)
        assertTrue(filter.accepts(key.asInstance()))
        assertFalse(filter.accepts(NavigationKeyFixtures.SimpleKey().asInstance()))
    }
}
