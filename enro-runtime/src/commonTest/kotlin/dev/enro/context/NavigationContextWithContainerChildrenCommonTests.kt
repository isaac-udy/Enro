package dev.enro.context

import dev.enro.test.fixtures.NavigationContextFixtures
import dev.enro.test.runEnroTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class NavigationContextWithContainerChildrenCommonTests(
    private val constructContext: () -> NavigationContext.WithContainerChildren<*>,
) {
    @Test
    fun `when single child is registered it becomes active`() = runEnroTest {
        val rootContext = constructContext()
        val child = NavigationContextFixtures.createContainerContext(rootContext)

        assertNull(rootContext.activeChild)

        rootContext.registerChild(child)

        assertEquals(child, rootContext.activeChild)
    }

    @Test
    fun `when single child is registered and becomes visible and then not visible it remains active`() = runEnroTest {
        val rootContext = constructContext()
        val child = NavigationContextFixtures.createContainerContext(rootContext)

        assertNull(rootContext.activeChild)
        rootContext.registerChild(child)
        rootContext.registerVisibility(child, true)
        rootContext.registerVisibility(child, false)
        assertEquals(child, rootContext.activeChild)
    }


    @Test
    fun `when child is registered as visible it becomes active`() = runEnroTest {
        val rootContext = constructContext()
        val child = NavigationContextFixtures.createContainerContext(rootContext)

        rootContext.registerChild(child)
        rootContext.registerVisibility(child, true)

        assertEquals(child, rootContext.activeChild)
    }

    @Test
    fun `when active child becomes not visible the first visible child becomes active`() = runEnroTest {
        val rootContext = constructContext()
        val child1 = NavigationContextFixtures.createContainerContext(rootContext)
        val child2 = NavigationContextFixtures.createContainerContext(rootContext)
        val child3 = NavigationContextFixtures.createContainerContext(rootContext)

        // Register all children
        rootContext.registerChild(child1)
        rootContext.registerChild(child2)
        rootContext.registerChild(child3)

        // First registered child should be active
        assertEquals(child1, rootContext.activeChild)

        // Make child2 and child3 visible
        rootContext.registerVisibility(child2, true)
        rootContext.registerVisibility(child3, true)

        // When child is registered as visible, if the previously active child was not visible, it
        // should become active
        assertEquals(child2, rootContext.activeChild)

        // Make child3 active
        rootContext.setActiveContainer(child3.id)
        assertEquals(child3, rootContext.activeChild)

        // When child3 becomes not visible, child2 should become active
        rootContext.registerVisibility(child3, false)
        assertEquals(child2, rootContext.activeChild)

        // Even when there is a visible container, setting the active container should still
        // respect the request for that container to become active
        rootContext.setActiveContainer(child1)
        assertEquals(child1, rootContext.activeChild)
    }

    @Test
    fun `when no children are visible activeChild is last visible child`() = runEnroTest {
        val rootContext = constructContext()
        val child1 = NavigationContextFixtures.createContainerContext(rootContext)
        val child2 = NavigationContextFixtures.createContainerContext(rootContext)

        rootContext.registerChild(child1)
        rootContext.registerChild(child2)

        rootContext.registerVisibility(child1, true)
        rootContext.registerVisibility(child2, true)

        rootContext.setActiveContainer(child2.id)
        assertEquals(child2, rootContext.activeChild)

        // Make all children not visible
        rootContext.registerVisibility(child2, false)
        rootContext.registerVisibility(child1, false)

        assertEquals(child1, rootContext.activeChild)
    }

    @Test
    fun `when child is unregistered and was active first visible child becomes active`() = runEnroTest {
        val rootContext = constructContext()
        val child1 = NavigationContextFixtures.createContainerContext(rootContext)
        val child2 = NavigationContextFixtures.createContainerContext(rootContext)
        val child3 = NavigationContextFixtures.createContainerContext(rootContext)

        rootContext.registerChild(child1)
        rootContext.registerChild(child2)
        rootContext.registerChild(child3)

        rootContext.registerVisibility(child1, true)
        rootContext.registerVisibility(child2, true)
        rootContext.registerVisibility(child3, true)

        rootContext.setActiveContainer(child3.id)
        assertEquals(child3, rootContext.activeChild)

        // Unregister the active child
        rootContext.unregisterChild(child3)

        // First visible child should become active
        assertEquals(child1, rootContext.activeChild)
    }

    @Test
    fun `when currently active child is not visible and new child becomes visible new child becomes active`() =
        runEnroTest {
            val rootContext = constructContext()
            val child1 = NavigationContextFixtures.createContainerContext(rootContext)
            val child2 = NavigationContextFixtures.createContainerContext(rootContext)

            rootContext.registerChild(child1)
            rootContext.registerChild(child2)

            // child1 is active by default (first registered)
            assertEquals(child1, rootContext.activeChild)

            // child1 is not visible by default, so when child2 becomes visible it should become active
            rootContext.registerVisibility(child2, true)
            assertEquals(child2, rootContext.activeChild)
        }

    @Test
    fun `setActiveContainer changes active child`() = runEnroTest {
        val rootContext = constructContext()
        val child1 = NavigationContextFixtures.createContainerContext(rootContext)
        val child2 = NavigationContextFixtures.createContainerContext(rootContext)

        rootContext.registerChild(child1)
        rootContext.registerChild(child2)

        assertEquals(child1, rootContext.activeChild)

        rootContext.setActiveContainer(child2.id)
        assertEquals(child2, rootContext.activeChild)

        rootContext.setActiveContainer(child1.id)
        assertEquals(child1, rootContext.activeChild)
    }

    @Test
    fun `children list contains all registered children regardless of visibility`() = runEnroTest {
        val rootContext = constructContext()
        val child1 = NavigationContextFixtures.createContainerContext(rootContext)
        val child2 = NavigationContextFixtures.createContainerContext(rootContext)
        val child3 = NavigationContextFixtures.createContainerContext(rootContext)

        rootContext.registerChild(child1)
        rootContext.registerChild(child2)
        rootContext.registerChild(child3)

        assertEquals(3, rootContext.children.size)
        assertEquals(setOf(child1, child2, child3), rootContext.children.toSet())

        // Change visibility doesn't affect children list
        rootContext.registerVisibility(child1, true)
        rootContext.registerVisibility(child2, false)
        rootContext.registerVisibility(child3, true)

        assertEquals(3, rootContext.children.size)
        assertEquals(setOf(child1, child2, child3), rootContext.children.toSet())
    }

    @Test
    fun `registerVisibility with unregistered child does nothing`() = runEnroTest {
        val rootContext = constructContext()
        val child = NavigationContextFixtures.createContainerContext(rootContext)

        // Try to register visibility without registering child first
        rootContext.registerVisibility(child, true)

        assertNull(rootContext.activeChild)
        assertEquals(0, rootContext.children.size)
    }

    @Test
    fun `multiple visibility changes maintain correct active child`() = runEnroTest {
        val rootContext = constructContext()
        val child1 = NavigationContextFixtures.createContainerContext(rootContext)
        val child2 = NavigationContextFixtures.createContainerContext(rootContext)
        val child3 = NavigationContextFixtures.createContainerContext(rootContext)

        rootContext.registerChild(child1)
        rootContext.registerChild(child2)
        rootContext.registerChild(child3)

        // Initially child1 is active
        assertEquals(child1, rootContext.activeChild)

        // Make child2 visible and active
        rootContext.registerVisibility(child2, true)
        rootContext.setActiveContainer(child2.id)
        assertEquals(child2, rootContext.activeChild)

        // Toggle visibility multiple times
        rootContext.registerVisibility(child1, true)
        rootContext.registerVisibility(child3, true)
        assertEquals(child2, rootContext.activeChild) // Should remain child2

        rootContext.registerVisibility(child2, false)
        assertEquals(child1, rootContext.activeChild) // Should switch to first visible

        rootContext.registerVisibility(child2, true)
        assertEquals(child1, rootContext.activeChild) // Should remain child1

        rootContext.setActiveContainer(child2.id)
        assertEquals(child2, rootContext.activeChild) // Explicitly set to child2
    }
}