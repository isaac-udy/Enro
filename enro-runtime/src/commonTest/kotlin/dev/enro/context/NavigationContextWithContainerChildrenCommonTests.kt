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

        rootContext.registerChild(child1)
        rootContext.registerChild(child2)
        rootContext.registerChild(child3)

        assertEquals(child1, rootContext.activeChild)

        rootContext.registerVisibility(child2, true)
        rootContext.registerVisibility(child3, true)

        // The active child1 is not visible, so the first child registered as visible becomes active.
        assertEquals(child2, rootContext.activeChild)

        rootContext.setActiveContainer(child3.id)
        assertEquals(child3, rootContext.activeChild)

        rootContext.registerVisibility(child3, false)
        assertEquals(child2, rootContext.activeChild)

        // setActiveContainer makes child1 active although child1 is not visible and child2 is visible.
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

        rootContext.unregisterChild(child3)

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

            assertEquals(child1, rootContext.activeChild)

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

        assertEquals(child1, rootContext.activeChild)

        rootContext.registerVisibility(child2, true)
        rootContext.setActiveContainer(child2.id)
        assertEquals(child2, rootContext.activeChild)

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