package dev.enro

import dev.enro.test.NavigationKeyFixtures
import dev.enro.test.fixtures.NavigationContextFixtures
import dev.enro.test.fixtures.NavigationDestinationFixtures
import dev.enro.test.runEnroTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Tests that lock down [NavigationOperation.SetBackstack] diff semantics.
 *
 * SetBackstack desugars to an `AggregateOperation` of `Open` + `Close`
 * operations based on a [NavigationTransition] diff. The key contract
 * the runtime depends on -- and that downstream destination tracking
 * relies on for state preservation -- is that retained instances pass
 * through `processOperations` as references to the original objects
 * (not copies, not new instances). Each test below asserts via
 * reference equality (`assertSame`) that retained backstack entries
 * survive a SetBackstack unchanged.
 */
class SetBackstackDiffTests {

    @Test
    fun `NavigationTransition computes opened closed and retained sets correctly`() {
        val a = NavigationKeyFixtures.SimpleKey().asInstance()
        val b = NavigationKeyFixtures.SimpleKey().asInstance()
        val c = NavigationKeyFixtures.SimpleKey().asInstance()
        val d = NavigationKeyFixtures.SimpleKey().asInstance()

        val transition = NavigationTransition(
            currentBackstack = backstackOf(a, b, c),
            targetBackstack = backstackOf(b, c, d),
        )

        assertEquals(listOf(d), transition.opened, "opened should be instances in target but not current")
        assertEquals(listOf(a), transition.closed, "closed should be instances in current but not target")
        assertEquals(setOf(b, c), transition.retained, "retained should be the intersection")
    }

    @Test
    fun `SetBackstack reorder preserves the same instance references`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container
        container.setFilter(acceptAll())

        val sourceDestination = NavigationDestinationFixtures.create(NavigationKeyFixtures.SimpleKey())
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, sourceDestination)

        val a = NavigationKeyFixtures.SimpleKey().asInstance()
        val b = NavigationKeyFixtures.SimpleKey().asInstance()
        val c = NavigationKeyFixtures.SimpleKey().asInstance()

        val initial = backstackOf(a, b, c)
        container.setBackstackDirect(initial)

        container.execute(
            destinationContext,
            NavigationOperation.SetBackstack(
                currentBackstack = initial,
                targetBackstack = backstackOf(c, b, a),
            ),
        )

        assertEquals(3, container.backstack.size)
        assertSame(c, container.backstack[0], "Reordered instance at position 0 should be the original c reference")
        assertSame(b, container.backstack[1], "Reordered instance at position 1 should be the original b reference")
        assertSame(a, container.backstack[2], "Reordered instance at position 2 should be the original a reference")
    }

    @Test
    fun `SetBackstack adding entries preserves existing instances and appends new ones`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container
        container.setFilter(acceptAll())

        val sourceDestination = NavigationDestinationFixtures.create(NavigationKeyFixtures.SimpleKey())
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, sourceDestination)

        val a = NavigationKeyFixtures.SimpleKey().asInstance()
        val newlyOpened = NavigationKeyFixtures.SimpleKey().asInstance()

        val initial = backstackOf(a)
        container.setBackstackDirect(initial)

        container.execute(
            destinationContext,
            NavigationOperation.SetBackstack(
                currentBackstack = initial,
                targetBackstack = backstackOf(a, newlyOpened),
            ),
        )

        assertEquals(2, container.backstack.size)
        assertSame(a, container.backstack[0], "Retained instance must keep its reference identity across the diff")
        assertSame(newlyOpened, container.backstack[1], "Newly opened instance should be appended at the end")
    }

    @Test
    fun `SetBackstack removing entries preserves the remaining instances and drops the removed`() = runEnroTest {
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container
        container.setFilter(acceptAll())

        val sourceDestination = NavigationDestinationFixtures.create(NavigationKeyFixtures.SimpleKey())
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, sourceDestination)

        val a = NavigationKeyFixtures.SimpleKey().asInstance()
        val b = NavigationKeyFixtures.SimpleKey().asInstance()
        val c = NavigationKeyFixtures.SimpleKey().asInstance()

        val initial = backstackOf(a, b, c)
        container.setBackstackDirect(initial)

        container.execute(
            destinationContext,
            NavigationOperation.SetBackstack(
                currentBackstack = initial,
                targetBackstack = backstackOf(a, c),
            ),
        )

        assertEquals(2, container.backstack.size)
        assertSame(a, container.backstack[0], "Retained instance a must keep its reference identity")
        assertSame(c, container.backstack[1], "Retained instance c must keep its reference identity")
        assertFalse(
            actual = container.backstack.any { it === b },
            message = "Removed instance b must no longer appear in the backstack",
        )
    }

    @Test
    fun `SetBackstack replacing the top entry retains the underlying entries and drops only the replaced one`() = runEnroTest {
        // The common list-detail "swap detail" gesture: [list, oldDetail]
        // becomes [list, newDetail]. The list entry must keep its reference
        // identity so its ViewModel / scope is preserved; the old detail
        // is dropped and the new detail appears in place.
        val rootContext = NavigationContextFixtures.createRootContext()
        val containerContext = NavigationContextFixtures.createContainerContext(rootContext)
        val container = containerContext.container
        container.setFilter(acceptAll())

        val sourceDestination = NavigationDestinationFixtures.create(NavigationKeyFixtures.SimpleKey())
        val destinationContext = NavigationContextFixtures.createDestinationContext(containerContext, sourceDestination)

        val list = NavigationKeyFixtures.SimpleKey().asInstance()
        val oldDetail = NavigationKeyFixtures.SimpleKey().asInstance()
        val newDetail = NavigationKeyFixtures.SimpleKey().asInstance()

        val initial = backstackOf(list, oldDetail)
        container.setBackstackDirect(initial)

        container.execute(
            destinationContext,
            NavigationOperation.SetBackstack(
                currentBackstack = initial,
                targetBackstack = backstackOf(list, newDetail),
            ),
        )

        assertEquals(2, container.backstack.size)
        assertSame(list, container.backstack[0], "List instance must retain its reference across the swap")
        assertSame(newDetail, container.backstack[1])
        assertTrue(
            actual = container.backstack.none { it === oldDetail },
            message = "Old detail instance must be gone after the swap",
        )
    }
}
