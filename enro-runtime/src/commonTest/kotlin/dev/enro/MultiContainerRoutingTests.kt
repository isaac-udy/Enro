package dev.enro

import dev.enro.handle.findContainerForOperation
import dev.enro.test.NavigationKeyFixtures
import dev.enro.test.fixtures.NavigationContextFixtures
import dev.enro.test.fixtures.NavigationDestinationFixtures
import dev.enro.test.runEnroTest
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertSame

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
}
