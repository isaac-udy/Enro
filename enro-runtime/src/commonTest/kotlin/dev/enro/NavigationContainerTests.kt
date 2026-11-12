package dev.enro

import dev.enro.context.ContainerContext
import dev.enro.context.NavigationContext
import dev.enro.interceptor.NavigationInterceptor
import dev.enro.interceptor.builder.navigationInterceptor
import dev.enro.test.EnroTest
import dev.enro.test.NavigationKeyFixtures
import dev.enro.test.fixtures.NavigationContextFixtures
import dev.enro.test.fixtures.NavigationDestinationFixtures
import dev.enro.test.runEnroTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class NavigationContainerTests {

    @Test
    fun `NavigationContainer accepts operations for keys in its backstack`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container

        val key1 = NavigationKeyFixtures.SimpleKey()
        val instance1 = key1.asInstance()
        val key2 = NavigationKeyFixtures.SimpleKey()
        val instance2 = key2.asInstance()

        assertFalse(container.accepts(containerContext, NavigationOperation.Close(instance1)))
        assertFalse(container.accepts(containerContext, NavigationOperation.Complete(instance1)))

        container.setBackstackDirect(backstackOf(instance1, instance2))

        assertTrue(container.accepts(containerContext, NavigationOperation.Close(instance1)))
        assertTrue(container.accepts(containerContext, NavigationOperation.Close(instance2)))
        assertTrue(container.accepts(containerContext, NavigationOperation.Complete(instance1)))
        assertTrue(container.accepts(containerContext, NavigationOperation.Complete(instance2)))

        val key3 = NavigationKeyFixtures.SimpleKey()
        val instance3 = key3.asInstance()
        assertFalse(container.accepts(containerContext, NavigationOperation.Close(instance3)))
        assertFalse(container.accepts(containerContext, NavigationOperation.Complete(instance3)))
    }

    @Test
    fun `NavigationContainer accepts open operations based on filter`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container

        val key1 = NavigationKeyFixtures.SimpleKey()
        val instance1 = key1.asInstance()

        // The default filter accepts no Open.
        assertFalse(container.accepts(containerContext, NavigationOperation.Open(instance1)))

        container.setFilter(acceptAll())
        assertTrue(container.accepts(containerContext, NavigationOperation.Open(instance1)))

        container.setFilter(acceptNone())
        assertFalse(container.accepts(containerContext, NavigationOperation.Open(instance1)))
    }

    @Test
    fun `NavigationContainer with fromChildrenOnly filter accepts operations from child contexts`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container

        val key = NavigationKeyFixtures.SimpleKey()
        val destination = NavigationDestinationFixtures.create(key)
        val childDestinationContext = NavigationContextFixtures.createDestinationContext(containerContext, destination)

        val filter = NavigationContainerFilter(fromChildrenOnly = true) { true }
        container.setFilter(filter)

        val instance = NavigationKeyFixtures.SimpleKey().asInstance()

        assertTrue(container.accepts(childDestinationContext, NavigationOperation.Open(instance)))
        assertTrue(container.accepts(containerContext, NavigationOperation.Open(instance)))
    }

    @Test
    fun `NavigationContainer with fromChildrenOnly filter rejects operations from non-child contexts`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container

        val otherContainerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val key = NavigationKeyFixtures.SimpleKey()
        val destination = NavigationDestinationFixtures.create(key)
        val nonChildDestinationContext =
            NavigationContextFixtures.createDestinationContext(otherContainerContext, destination)

        val filter = NavigationContainerFilter(fromChildrenOnly = true) { true }
        container.setFilter(filter)

        val instance = NavigationKeyFixtures.SimpleKey().asInstance()

        assertFalse(container.accepts(nonChildDestinationContext, NavigationOperation.Open(instance)))
        assertFalse(container.accepts(otherContainerContext, NavigationOperation.Open(instance)))
        assertFalse(container.accepts(rootContext, NavigationOperation.Open(instance)))
    }

    @Test
    fun `NavigationContainer with fromChildrenOnly false accepts operations from any context`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container

        val key = NavigationKeyFixtures.SimpleKey()
        val destination = NavigationDestinationFixtures.create(key)
        val childDestinationContext = NavigationContextFixtures.createDestinationContext(containerContext, destination)

        val otherContainerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val nonChildDestinationContext =
            NavigationContextFixtures.createDestinationContext(otherContainerContext, destination)

        val filter = NavigationContainerFilter(fromChildrenOnly = false) { true }
        container.setFilter(filter)

        val instance = NavigationKeyFixtures.SimpleKey().asInstance()

        assertTrue(container.accepts(childDestinationContext, NavigationOperation.Open(instance)))
        assertTrue(container.accepts(containerContext, NavigationOperation.Open(instance)))
        assertTrue(container.accepts(nonChildDestinationContext, NavigationOperation.Open(instance)))
        assertTrue(container.accepts(otherContainerContext, NavigationOperation.Open(instance)))
        assertTrue(container.accepts(rootContext, NavigationOperation.Open(instance)))
    }

    @Test
    fun `NavigationContainer with fromChildrenOnly filter and predicate applies both conditions`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container

        val key = NavigationKeyFixtures.SimpleKey()
        val destination = NavigationDestinationFixtures.create(key)
        val childDestinationContext = NavigationContextFixtures.createDestinationContext(containerContext, destination)

        val otherContainerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val nonChildDestinationContext =
            NavigationContextFixtures.createDestinationContext(otherContainerContext, destination)

        val acceptedKey = NavigationKeyFixtures.SimpleKey()
        val rejectedKey = NavigationKeyFixtures.SimpleKey()

        val filter = NavigationContainerFilter(fromChildrenOnly = true) { it.key == acceptedKey }
        container.setFilter(filter)

        val acceptedInstance = acceptedKey.asInstance()
        val rejectedInstance = rejectedKey.asInstance()

        assertTrue(container.accepts(childDestinationContext, NavigationOperation.Open(acceptedInstance)))

        assertFalse(container.accepts(childDestinationContext, NavigationOperation.Open(rejectedInstance)))

        assertFalse(container.accepts(nonChildDestinationContext, NavigationOperation.Open(acceptedInstance)))

        assertFalse(container.accepts(nonChildDestinationContext, NavigationOperation.Open(rejectedInstance)))
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

        container.setBackstackDirect(backstackOf(instance1, instance2))
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

        container.setBackstackDirect(backstackOf(instance1, instance2))
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

        container.setBackstackDirect(backstackOf(instance1))

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
        container.setBackstackDirect(backstackOf(instance))

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
        container.setBackstackDirect(backstackOf(instance))

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
    fun `Multiple EmptyInterceptors any deny wins and side effects from every deny run`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container

        val sourceDestination = NavigationDestinationFixtures.create(NavigationKeyFixtures.SimpleKey())
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, sourceDestination)

        val instance = NavigationKeyFixtures.SimpleKey().asInstance()
        container.setBackstackDirect(backstackOf(instance))

        var denySideEffectRan = false
        var allowInterceptorCalled = false

        val denyInterceptor = object : NavigationContainer.EmptyInterceptor() {
            override fun onEmpty(transition: NavigationTransition): Result {
                return denyEmptyAnd { denySideEffectRan = true }
            }
        }
        val allowInterceptor = object : NavigationContainer.EmptyInterceptor() {
            override fun onEmpty(transition: NavigationTransition): Result {
                allowInterceptorCalled = true
                return allowEmpty()
            }
        }
        container.addEmptyInterceptor(denyInterceptor)
        container.addEmptyInterceptor(allowInterceptor)

        container.execute(destinationContext, NavigationOperation.Close(instance))

        assertTrue(allowInterceptorCalled, "Allow interceptor should still be consulted")
        assertTrue(denySideEffectRan, "Side effect from a DenyEmpty result should run even when another interceptor returns AllowEmpty")
        assertEquals(1, container.backstack.size, "A single DenyEmpty wins over AllowEmpty — backstack should not be emptied")
    }

    @Test
    fun `Controller-level interceptors run after container-level interceptors`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container
        container.setFilter(acceptAll())

        val sourceDestination = NavigationDestinationFixtures.create(NavigationKeyFixtures.SimpleKey())
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, sourceDestination)

        val ordering = mutableListOf<String>()
        val containerInterceptor = navigationInterceptor {
            onOpened<NavigationKeyFixtures.SimpleKey> {
                ordering += "container"
                continueWithOpen()
            }
        }
        val controllerInterceptor = navigationInterceptor {
            onOpened<NavigationKeyFixtures.SimpleKey> {
                ordering += "controller"
                continueWithOpen()
            }
        }
        container.addInterceptor(containerInterceptor)
        EnroTest.getCurrentNavigationController().interceptors.addInterceptor(controllerInterceptor)

        container.execute(
            destinationContext,
            NavigationOperation.Open(NavigationKeyFixtures.SimpleKey().asInstance()),
        )

        assertEquals(listOf("container", "controller"), ordering)
    }

    @Test
    fun `Open of an instance already in the backstack reorders without firing onOpened interceptor`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container
        container.setFilter(acceptAll())

        val sourceDestination = NavigationDestinationFixtures.create(NavigationKeyFixtures.SimpleKey())
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, sourceDestination)

        val existing = NavigationKeyFixtures.SimpleKey().asInstance()
        val top = NavigationKeyFixtures.SimpleKey().asInstance()
        container.setBackstackDirect(backstackOf(existing, top))

        var onOpenedCount = 0
        val interceptor = navigationInterceptor {
            onOpened<NavigationKeyFixtures.SimpleKey> {
                onOpenedCount++
                continueWithOpen()
            }
        }
        container.addInterceptor(interceptor)

        container.execute(destinationContext, NavigationOperation.Open(existing))

        assertEquals(
            expected = 0,
            actual = onOpenedCount,
            message = "Reordering an already-present instance should bypass onOpened entirely",
        )
        assertEquals(2, container.backstack.size)
        assertEquals(top.id, container.backstack[0].id, "previous top should drop to the bottom")
        assertEquals(existing.id, container.backstack[1].id, "reordered instance should move to the top")
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
        container.setBackstackDirect(backstackOf(instance))

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
    fun `Interceptor returning AggregateOperation containing the original op does not recurse`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container
        container.setFilter(acceptAll())

        val anchorInstance = NavigationKeyFixtures.SimpleKey().asInstance()
        val existingDetailInstance = NavigationKeyFixtures.SimpleKey().asInstance()
        container.setBackstackDirect(backstackOf(anchorInstance, existingDetailInstance))

        val sourceDestination = NavigationDestinationFixtures.create(NavigationKeyFixtures.SimpleKey())
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, sourceDestination)

        val newDetailInstance = NavigationKeyFixtures.SimpleKey().asInstance()

        var interceptCount = 0
        val interceptor = navigationInterceptor {
            onOpened<NavigationKeyFixtures.SimpleKey> {
                // The interceptor handles the Open of the new detail only. A second call for the
                // Open of the new detail raises interceptCount above 1.
                if (instance.id != newDetailInstance.id) continueWithOpen()
                interceptCount++
                replaceWith(
                    NavigationOperation.AggregateOperation(
                        instance.asOpenOperation(),
                        existingDetailInstance.asCloseOperation(),
                    )
                )
            }
        }
        container.addInterceptor(interceptor)

        container.execute(destinationContext, NavigationOperation.Open(newDetailInstance))

        assertEquals(
            expected = 1,
            actual = interceptCount,
            message = "Interceptor must fire exactly once — re-feeding the original op would re-invoke it.",
        )
        assertEquals(2, container.backstack.size)
        assertEquals(anchorInstance.id, container.backstack[0].id)
        assertEquals(newDetailInstance.id, container.backstack[1].id)
    }

    @Test
    fun `OnNavigationKeyOpenedScope exposes backstack fromContext and containerContext`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container
        container.setFilter(acceptAll())

        val existingInstance = NavigationKeyFixtures.SimpleKey().asInstance()
        container.setBackstackDirect(backstackOf(existingInstance))

        val sourceDestination = NavigationDestinationFixtures.create(NavigationKeyFixtures.SimpleKey())
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, sourceDestination)

        var capturedBackstack: NavigationBackstack? = null
        var capturedFromContext: NavigationContext<*, *>? = null
        var capturedContainerContext: ContainerContext? = null

        val interceptor = navigationInterceptor {
            onOpened<NavigationKeyFixtures.SimpleKey> {
                capturedBackstack = backstack
                capturedFromContext = fromContext
                capturedContainerContext = containerContext
                continueWithOpen()
            }
        }
        container.addInterceptor(interceptor)

        val triggerInstance = NavigationKeyFixtures.SimpleKey().asInstance()
        container.execute(destinationContext, NavigationOperation.Open(triggerInstance))

        assertEquals(
            expected = listOf(existingInstance.id),
            actual = capturedBackstack?.map { it.id },
            message = "backstack should reflect the state PRIOR to the new Open operation",
        )
        assertSame(destinationContext, capturedFromContext, "fromContext should be the originating destination context")
        assertSame(containerContext, capturedContainerContext, "containerContext should be the target container's context")
    }

    @Test
    fun `OnNavigationKeyClosedScope exposes backstack fromContext and containerContext`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container

        val instance = NavigationKeyFixtures.SimpleKey().asInstance()
        container.setBackstackDirect(backstackOf(instance))

        val sourceDestination = NavigationDestinationFixtures.create(NavigationKeyFixtures.SimpleKey())
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, sourceDestination)

        var capturedBackstack: NavigationBackstack? = null
        var capturedFromContext: NavigationContext<*, *>? = null
        var capturedContainerContext: ContainerContext? = null

        val interceptor = navigationInterceptor {
            onClosed<NavigationKeyFixtures.SimpleKey> {
                capturedBackstack = backstack
                capturedFromContext = fromContext
                capturedContainerContext = containerContext
                continueWithClose()
            }
        }
        container.addInterceptor(interceptor)

        container.execute(destinationContext, NavigationOperation.Close(instance))

        assertEquals(listOf(instance.id), capturedBackstack?.map { it.id })
        assertSame(destinationContext, capturedFromContext)
        assertSame(containerContext, capturedContainerContext)
    }

    @Test
    fun `OnNavigationKeyCompletedScope exposes backstack fromContext and containerContext`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container

        val instance = NavigationKeyFixtures.SimpleKey().asInstance()
        container.setBackstackDirect(backstackOf(instance))

        val sourceDestination = NavigationDestinationFixtures.create(NavigationKeyFixtures.SimpleKey())
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, sourceDestination)

        var capturedBackstack: NavigationBackstack? = null
        var capturedFromContext: NavigationContext<*, *>? = null
        var capturedContainerContext: ContainerContext? = null

        val interceptor = navigationInterceptor {
            onCompleted<NavigationKeyFixtures.SimpleKey> {
                capturedBackstack = backstack
                capturedFromContext = fromContext
                capturedContainerContext = containerContext
                continueWithComplete()
            }
        }
        container.addInterceptor(interceptor)

        container.execute(destinationContext, NavigationOperation.Complete(instance))

        assertEquals(listOf(instance.id), capturedBackstack?.map { it.id })
        assertSame(destinationContext, capturedFromContext)
        assertSame(containerContext, capturedContainerContext)
    }

    @Test
    fun `replaceWith operation on OnNavigationKeyOpenedScope can rewrite Open into a SetBackstack`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container
        container.setFilter(acceptAll())

        val firstInstance = NavigationKeyFixtures.SimpleKey().asInstance()
        val secondInstance = NavigationKeyFixtures.SimpleKey().asInstance()
        container.setBackstackDirect(backstackOf(firstInstance, secondInstance))

        val sourceDestination = NavigationDestinationFixtures.create(NavigationKeyFixtures.SimpleKey())
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, sourceDestination)

        val triggerInstance = NavigationKeyFixtures.SimpleKey().asInstance()
        val firstReplacement = NavigationKeyFixtures.SimpleKey().asInstance()
        val secondReplacement = NavigationKeyFixtures.SimpleKey().asInstance()

        val interceptor = navigationInterceptor {
            onOpened<NavigationKeyFixtures.SimpleKey> {
                // The interceptor replaces the trigger only. The Opens produced by the
                // SetBackstack continue, so the interceptor does not replace its own output.
                if (instance.id != triggerInstance.id) continueWithOpen()
                replaceWith(
                    NavigationOperation.SetBackstack(
                        currentBackstack = backstack,
                        targetBackstack = backstackOf(firstReplacement, secondReplacement),
                    )
                )
            }
        }
        container.addInterceptor(interceptor)

        container.execute(destinationContext, NavigationOperation.Open(triggerInstance))

        assertEquals(2, container.backstack.size)
        assertEquals(firstReplacement.id, container.backstack[0].id)
        assertEquals(secondReplacement.id, container.backstack[1].id)
    }

    @Test
    fun `Re-entrant execute from within an interceptor throws IllegalStateException`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container
        container.setFilter(acceptAll())

        val sourceDestination = NavigationDestinationFixtures.create(NavigationKeyFixtures.SimpleKey())
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, sourceDestination)

        val intruderInstance = NavigationKeyFixtures.SimpleKey().asInstance()
        val interceptor = navigationInterceptor {
            onOpened<NavigationKeyFixtures.SimpleKey> {
                containerContext.container.execute(
                    destinationContext,
                    NavigationOperation.Open(intruderInstance),
                )
                continueWithOpen()
            }
        }
        container.addInterceptor(interceptor)

        val triggerInstance = NavigationKeyFixtures.SimpleKey().asInstance()
        val error = assertFailsWith<IllegalStateException> {
            container.execute(destinationContext, NavigationOperation.Open(triggerInstance))
        }
        assertTrue(
            actual = error.message?.contains("navigationInterceptor") == true,
            message = "Error should call out the interceptor as the cause; was: ${error.message}",
        )
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

        val otherContainer = NavigationContextFixtures.createContainerContext(rootContext)
        rootContext.registerChild(otherContainer)
        rootContext.setActiveContainer(otherContainer.id)

        assertEquals(otherContainer, rootContext.activeChild)

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

        container.setBackstackDirect(backstackOf(instance1, instance2, instance3))
        assertEquals(3, container.backstack.size)
        assertEquals(instance1, container.backstack[0])
        assertEquals(instance2, container.backstack[1])
        assertEquals(instance3, container.backstack[2])

        container.execute(destinationContext, NavigationOperation.Open(instance1))

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

        container.setBackstackDirect(backstackOf(instance1, instance2, instance3))

        container.execute(destinationContext, NavigationOperation.Open(instance2))

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

        container.setBackstackDirect(backstackOf(instance1, instance2, instance3))

        container.execute(destinationContext, NavigationOperation.Open(instance3))

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

        container.setBackstackDirect(backstackOf(instance1, instance2, instance3))

        val aggregateOperation = NavigationOperation.AggregateOperation(
            listOf(
                NavigationOperation.Open(instance1), // Move to top
                NavigationOperation.Open(instance4), // Add new
                NavigationOperation.Open(instance2), // Move to top
            )
        )

        container.execute(destinationContext, aggregateOperation)

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

        container.setBackstackDirect(backstackOf(instance))

        container.execute(destinationContext, NavigationOperation.Open(instance))

        assertEquals(1, container.backstack.size)
        assertEquals(instance, container.backstack[0])
    }
}
