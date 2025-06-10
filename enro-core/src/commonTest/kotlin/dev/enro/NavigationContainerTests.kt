package dev.enro

import dev.enro.context.ContainerContext
import dev.enro.context.NavigationContext
import dev.enro.interceptor.NavigationInterceptor
import dev.enro.interceptor.builder.navigationInterceptor
import dev.enro.test.EnroTest
import dev.enro.test.NavigationContextFixtures
import dev.enro.test.NavigationDestinationFixtures
import dev.enro.test.NavigationKeyFixtures
import dev.enro.test.runEnroTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NavigationContainerTests {

    @Test
    fun `NavigationContainer accepts operations for keys in its backstack`() = runEnroTest {
        val controller = EnroTest.getCurrentNavigationController()
        val container = NavigationContainer(
            key = NavigationContainer.Key("test"),
            controller = controller,
        )

        val key1 = NavigationKeyFixtures.SimpleKey()
        val instance1 = key1.asInstance()
        val key2 = NavigationKeyFixtures.SimpleKey()
        val instance2 = key2.asInstance()

        // Empty container should not accept close operations
        assertFalse(container.accepts(NavigationOperation.Close(instance1)))
        assertFalse(container.accepts(NavigationOperation.Complete(instance1)))

        // Add instances to backstack
        container.setBackstackDirect(listOf(instance1, instance2))

        // Container should accept operations for instances in backstack
        assertTrue(container.accepts(NavigationOperation.Close(instance1)))
        assertTrue(container.accepts(NavigationOperation.Close(instance2)))
        assertTrue(container.accepts(NavigationOperation.Complete(instance1)))
        assertTrue(container.accepts(NavigationOperation.Complete(instance2)))

        // Container should not accept operations for instances not in backstack
        val key3 = NavigationKeyFixtures.SimpleKey()
        val instance3 = key3.asInstance()
        assertFalse(container.accepts(NavigationOperation.Close(instance3)))
        assertFalse(container.accepts(NavigationOperation.Complete(instance3)))
    }

    @Test
    fun `NavigationContainer accepts open operations based on filter`() = runEnroTest {
        val controller = EnroTest.getCurrentNavigationController()
        val container = NavigationContainer(
            key = NavigationContainer.Key("test"),
            controller = controller,
        )

        val key1 = NavigationKeyFixtures.SimpleKey()
        val instance1 = key1.asInstance()

        // By default, container accepts no open operations
        assertFalse(container.accepts(NavigationOperation.Open(instance1)))

        // Set filter to accept all
        container.setFilter(acceptAll())
        assertTrue(container.accepts(NavigationOperation.Open(instance1)))

        // Set filter to accept none
        container.setFilter(acceptNone())
        assertFalse(container.accepts(NavigationOperation.Open(instance1)))
    }

    @Test
    fun `Open operation adds instance to backstack`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container
        container.setFilter(acceptAll())

        val key = NavigationKeyFixtures.SimpleKey()
        val destination = NavigationDestinationFixtures.create(key)
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, destination)

        val instance = key.asInstance()

        assertEquals(0, container.backstack.size)

        container.execute(destinationContext, NavigationOperation.Open(instance))

        assertEquals(1, container.backstack.size)
        assertEquals(instance, container.backstack.first())
    }

    @Test
    fun `Close operation removes instance from backstack`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container

        val key1 = NavigationKeyFixtures.SimpleKey()
        val destination = NavigationDestinationFixtures.create(key1)
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, destination)

        val instance1 = NavigationKeyFixtures.SimpleKey().asInstance()
        val instance2 = NavigationKeyFixtures.SimpleKey().asInstance()

        container.setBackstackDirect(listOf(instance1, instance2))
        assertEquals(2, container.backstack.size)

        container.execute(destinationContext, NavigationOperation.Close(instance1))

        assertEquals(1, container.backstack.size)
        assertEquals(instance2, container.backstack.first())
    }

    @Test
    fun `Complete operation removes instance from backstack`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container

        val key1 = NavigationKeyFixtures.SimpleKey()
        val destination = NavigationDestinationFixtures.create(key1)
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, destination)

        val instance1 = NavigationKeyFixtures.SimpleKey().asInstance()
        val instance2 = NavigationKeyFixtures.SimpleKey().asInstance()

        container.setBackstackDirect(listOf(instance1, instance2))
        assertEquals(2, container.backstack.size)

        container.execute(destinationContext, NavigationOperation.Complete(instance1))

        assertEquals(1, container.backstack.size)
        assertEquals(instance2, container.backstack.first())
    }

    @Test
    fun `Multiple operations are processed in order`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container
        container.setFilter(acceptAll())

        val key = NavigationKeyFixtures.SimpleKey()
        val destination = NavigationDestinationFixtures.create(key)
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, destination)

        val instance1 = NavigationKeyFixtures.SimpleKey().asInstance()
        val instance2 = NavigationKeyFixtures.SimpleKey().asInstance()
        val instance3 = NavigationKeyFixtures.SimpleKey().asInstance()

        container.setBackstackDirect(listOf(instance1))

        val aggregateOperation = NavigationOperation.AggregateOperation(
            listOf(
                NavigationOperation.Open(instance2),
                NavigationOperation.Open(instance3),
                NavigationOperation.Close(instance1),
            )
        )

        container.execute(destinationContext, aggregateOperation)

        assertEquals(2, container.backstack.size)
        assertEquals(instance2, container.backstack[0])
        assertEquals(instance3, container.backstack[1])
    }

    @Test
    fun `NavigationInterceptor can modify open operations`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container
        container.setFilter(acceptAll())

        val originalKey = NavigationKeyFixtures.SimpleKey()
        val destination = NavigationDestinationFixtures.create(originalKey)
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, destination)

        val replacementKey = NavigationKeyFixtures.SimpleKey()

        var interceptorCalled = false
        val interceptor = navigationInterceptor {
            onOpened<NavigationKeyFixtures.SimpleKey> {
                if (key !== originalKey) return@onOpened
                interceptorCalled = true
                replaceWith(replacementKey)
            }
        }

        container.addInterceptor(interceptor)

        container.execute(destinationContext, NavigationOperation.Open(originalKey.asInstance()))

        assertTrue(interceptorCalled)
        assertEquals(1, container.backstack.size)
        assertEquals(replacementKey, container.backstack.first().key)
    }

    @Test
    fun `NavigationInterceptor can cancel operations`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container
        container.setFilter(acceptAll())

        val key = NavigationKeyFixtures.SimpleKey()
        val destination = NavigationDestinationFixtures.create(key)
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, destination)

        var interceptorCalled = false
        val interceptor = navigationInterceptor {
            onOpened<NavigationKeyFixtures.SimpleKey> {
                interceptorCalled = true
                cancel()
            }
        }

        container.addInterceptor(interceptor)

        container.execute(destinationContext, NavigationOperation.Open(key.asInstance()))

        assertTrue(interceptorCalled)
        assertEquals(0, container.backstack.size)
    }

    @Test
    fun `EmptyInterceptor prevents container from becoming empty`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container

        val key = NavigationKeyFixtures.SimpleKey()
        val destination = NavigationDestinationFixtures.create(key)
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, destination)

        val instance = NavigationKeyFixtures.SimpleKey().asInstance()
        container.setBackstackDirect(listOf(instance))

        var emptyInterceptorCalled = false
        val emptyInterceptor = object : NavigationContainer.EmptyInterceptor() {
            override fun onEmpty(transition: NavigationTransition): Result {
                emptyInterceptorCalled = true
                return denyEmpty()
            }
        }

        container.addEmptyInterceptor(emptyInterceptor)

        container.execute(destinationContext, NavigationOperation.Close(instance))

        assertTrue(emptyInterceptorCalled)
        assertEquals(1, container.backstack.size) // Container should still have the instance
    }

    @Test
    fun `EmptyInterceptor allows container to become empty`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container

        val key = NavigationKeyFixtures.SimpleKey()
        val destination = NavigationDestinationFixtures.create(key)
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, destination)

        val instance = NavigationKeyFixtures.SimpleKey().asInstance()
        container.setBackstackDirect(listOf(instance))

        var emptyInterceptorCalled = false
        val emptyInterceptor = object : NavigationContainer.EmptyInterceptor() {
            override fun onEmpty(transition: NavigationTransition): Result {
                emptyInterceptorCalled = true
                return allowEmpty()
            }
        }

        container.addEmptyInterceptor(emptyInterceptor)

        container.execute(destinationContext, NavigationOperation.Close(instance))

        assertTrue(emptyInterceptorCalled)
        assertEquals(0, container.backstack.size)
    }

    @Test
    fun `EmptyInterceptor with side effect`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container

        val key = NavigationKeyFixtures.SimpleKey()
        val destination = NavigationDestinationFixtures.create(key)
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, destination)

        val instance = NavigationKeyFixtures.SimpleKey().asInstance()
        container.setBackstackDirect(listOf(instance))

        var sideEffectExecuted = false
        val emptyInterceptor = object : NavigationContainer.EmptyInterceptor() {
            override fun onEmpty(transition: NavigationTransition): Result {
                return denyEmptyAnd {
                    sideEffectExecuted = true
                }
            }
        }

        container.addEmptyInterceptor(emptyInterceptor)

        container.execute(destinationContext, NavigationOperation.Close(instance))

        assertTrue(sideEffectExecuted)
        assertEquals(1, container.backstack.size) // Container should still have the instance
    }

    @Test
    fun `Multiple interceptors are applied in order`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container
        container.setFilter(acceptAll())

        val key1 = NavigationKeyFixtures.SimpleKey()
        val destination = NavigationDestinationFixtures.create(key1)
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, destination)

        val key2 = NavigationKeyFixtures.SimpleKey()
        val key3 = NavigationKeyFixtures.SimpleKey()

        var interceptor1Called = false
        var interceptor2Called = false

        val interceptor1 = navigationInterceptor {
            onOpened<NavigationKeyFixtures.SimpleKey> {
                if (key == key1) {
                    interceptor1Called = true
                    replaceWith(key2)
                }
            }
        }

        val interceptor2 = navigationInterceptor {
            onOpened<NavigationKeyFixtures.SimpleKey> {
                interceptor2Called = true
                if (key == key2) {
                    replaceWith(key3)
                } else {
                    continueWithOpen()
                }
            }
        }

        container.addInterceptor(interceptor1)
        container.addInterceptor(interceptor2)

        container.execute(destinationContext, NavigationOperation.Open(key1.asInstance()))

        assertTrue(interceptor1Called)
        assertTrue(interceptor2Called)
        assertEquals(1, container.backstack.size)
        assertEquals(key3, container.backstack.first().key)
    }

    @Test
    fun `SideEffect operations are executed`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container

        val key = NavigationKeyFixtures.SimpleKey()
        val destination = NavigationDestinationFixtures.create(key)
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, destination)

        var sideEffectExecuted = false
        val sideEffect = NavigationOperation.SideEffect {
            sideEffectExecuted = true
        }

        container.execute(destinationContext, sideEffect)

        assertTrue(sideEffectExecuted)
    }

    @Test
    fun `Interceptor beforeIntercept can modify operation list`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container
        container.setFilter(acceptAll())

        val key1 = NavigationKeyFixtures.SimpleKey()
        val destination = NavigationDestinationFixtures.create(key1)
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, destination)

        val key2 = NavigationKeyFixtures.SimpleKey()

        val interceptor = object : NavigationInterceptor() {
            override fun beforeIntercept(
                fromContext: NavigationContext<*, *>,
                containerContext: ContainerContext,
                operations: List<NavigationOperation.RootOperation>,
            ): List<NavigationOperation.RootOperation> {
                // Add an extra operation
                return operations + NavigationOperation.Open(key2.asInstance())
            }
        }

        container.addInterceptor(interceptor)

        container.execute(destinationContext, NavigationOperation.Open(key1.asInstance()))

        assertEquals(2, container.backstack.size)
        assertEquals(key1, container.backstack[0].key)
        assertEquals(key2, container.backstack[1].key)
    }

    @Test
    fun `Remove interceptor stops it from being called`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container
        container.setFilter(acceptAll())

        val key = NavigationKeyFixtures.SimpleKey()
        val destination = NavigationDestinationFixtures.create(key)
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, destination)

        var interceptorCalled = false
        val interceptor = navigationInterceptor {
            onOpened<NavigationKeyFixtures.SimpleKey> {
                interceptorCalled = true
                cancel()
            }
        }

        container.addInterceptor(interceptor)
        container.removeInterceptor(interceptor)

        container.execute(destinationContext, NavigationOperation.Open(key.asInstance()))

        assertFalse(interceptorCalled)
        assertEquals(1, container.backstack.size)
    }

    @Test
    fun `Container requests active in root when backstack changes`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container
        container.setFilter(acceptAll())

        rootContext.registerChild(containerContext)

        val key = NavigationKeyFixtures.SimpleKey()
        val destination = NavigationDestinationFixtures.create(key)
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, destination)

        // Create another container to verify active switching
        val otherContainer = NavigationContextFixtures.createContainerContext(rootContext)
        rootContext.registerChild(otherContainer)
        rootContext.setActiveContainer(otherContainer.id)

        assertEquals(otherContainer, rootContext.activeChild)

        // Execute operation should make this container active
        container.execute(destinationContext, NavigationOperation.Open(key.asInstance()))

        assertEquals(containerContext, rootContext.activeChild)
    }

    @Test
    fun `Opening existing instance reorders backstack instead of duplicating`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container
        container.setFilter(acceptAll())

        val key = NavigationKeyFixtures.SimpleKey()
        val destination = NavigationDestinationFixtures.create(key)
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, destination)

        val instance1 = NavigationKeyFixtures.SimpleKey().asInstance()
        val instance2 = NavigationKeyFixtures.SimpleKey().asInstance()
        val instance3 = NavigationKeyFixtures.SimpleKey().asInstance()

        // Set initial backstack
        container.setBackstackDirect(listOf(instance1, instance2, instance3))
        assertEquals(3, container.backstack.size)
        assertEquals(instance1, container.backstack[0])
        assertEquals(instance2, container.backstack[1])
        assertEquals(instance3, container.backstack[2])

        // Open instance1 which is already at position 0
        container.execute(destinationContext, NavigationOperation.Open(instance1))

        // Backstack should be reordered with instance1 moved to the top
        assertEquals(3, container.backstack.size)
        assertEquals(instance2, container.backstack[0])
        assertEquals(instance3, container.backstack[1])
        assertEquals(instance1, container.backstack[2])
    }

    @Test
    fun `Opening existing instance from middle of backstack moves it to top`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container
        container.setFilter(acceptAll())

        val key = NavigationKeyFixtures.SimpleKey()
        val destination = NavigationDestinationFixtures.create(key)
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, destination)

        val instance1 = NavigationKeyFixtures.SimpleKey().asInstance()
        val instance2 = NavigationKeyFixtures.SimpleKey().asInstance()
        val instance3 = NavigationKeyFixtures.SimpleKey().asInstance()

        // Set initial backstack
        container.setBackstackDirect(listOf(instance1, instance2, instance3))

        // Open instance2 which is in the middle
        container.execute(destinationContext, NavigationOperation.Open(instance2))

        // Backstack should be reordered with instance2 moved to the top
        assertEquals(3, container.backstack.size)
        assertEquals(instance1, container.backstack[0])
        assertEquals(instance3, container.backstack[1])
        assertEquals(instance2, container.backstack[2])
    }

    @Test
    fun `Opening existing instance that is already at top does not change backstack`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container
        container.setFilter(acceptAll())

        val key = NavigationKeyFixtures.SimpleKey()
        val destination = NavigationDestinationFixtures.create(key)
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, destination)

        val instance1 = NavigationKeyFixtures.SimpleKey().asInstance()
        val instance2 = NavigationKeyFixtures.SimpleKey().asInstance()
        val instance3 = NavigationKeyFixtures.SimpleKey().asInstance()

        // Set initial backstack
        container.setBackstackDirect(listOf(instance1, instance2, instance3))

        // Open instance3 which is already at the top
        container.execute(destinationContext, NavigationOperation.Open(instance3))

        // Backstack should remain unchanged
        assertEquals(3, container.backstack.size)
        assertEquals(instance1, container.backstack[0])
        assertEquals(instance2, container.backstack[1])
        assertEquals(instance3, container.backstack[2])
    }

    @Test
    fun `Multiple operations with existing instances reorder correctly`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container
        container.setFilter(acceptAll())

        val key = NavigationKeyFixtures.SimpleKey()
        val destination = NavigationDestinationFixtures.create(key)
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, destination)

        val instance1 = NavigationKeyFixtures.SimpleKey().asInstance()
        val instance2 = NavigationKeyFixtures.SimpleKey().asInstance()
        val instance3 = NavigationKeyFixtures.SimpleKey().asInstance()
        val instance4 = NavigationKeyFixtures.SimpleKey().asInstance()

        // Set initial backstack
        container.setBackstackDirect(listOf(instance1, instance2, instance3))

        val aggregateOperation = NavigationOperation.AggregateOperation(
            listOf(
                NavigationOperation.Open(instance1), // Move to top
                NavigationOperation.Open(instance4), // Add new
                NavigationOperation.Open(instance2), // Move to top
            )
        )

        container.execute(destinationContext, aggregateOperation)

        // Expected order: instance3, instance1, instance4, instance2
        assertEquals(4, container.backstack.size)
        assertEquals(instance3, container.backstack[0])
        assertEquals(instance1, container.backstack[1])
        assertEquals(instance4, container.backstack[2])
        assertEquals(instance2, container.backstack[3])
    }

    @Test
    fun `Opening existing instance with single item backstack does nothing`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container
        container.setFilter(acceptAll())

        val key = NavigationKeyFixtures.SimpleKey()
        val destination = NavigationDestinationFixtures.create(key)
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, destination)

        val instance = NavigationKeyFixtures.SimpleKey().asInstance()

        // Set backstack with single item
        container.setBackstackDirect(listOf(instance))

        // Open the same instance
        container.execute(destinationContext, NavigationOperation.Open(instance))

        // Backstack should remain unchanged
        assertEquals(1, container.backstack.size)
        assertEquals(instance, container.backstack[0])
    }
}
