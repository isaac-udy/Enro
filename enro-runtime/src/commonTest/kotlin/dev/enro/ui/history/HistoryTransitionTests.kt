package dev.enro.ui.history

import dev.enro.NavigationContainer
import dev.enro.NavigationKey
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.emptyBackstack
import dev.enro.test.NavigationKeyFixtures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Locks down the push-versus-replace line the web history plugin draws between two recorded
 * states, and the equality that decides whether a state is one it has already seen. Both are
 * pure functions of [ContainerNode], so they are tested without a browser.
 */
class HistoryTransitionTests {

    private val root = NavigationContainer.Key("root")
    private val homeTab = NavigationContainer.Key("home-tab")
    private val predictTab = NavigationContainer.Key("predict-tab")

    private val shell = NavigationKeyFixtures.SimpleKey().asInstance()
    private val home = NavigationKeyFixtures.SimpleKey().asInstance()
    private val standings = NavigationKeyFixtures.SimpleKey().asInstance()
    private val predict = NavigationKeyFixtures.SimpleKey().asInstance()
    private val round = NavigationKeyFixtures.SimpleKey().asInstance()

    private fun node(
        key: NavigationContainer.Key,
        vararg backstack: NavigationKey.Instance<*>,
        children: List<ContainerNode> = emptyList(),
        activeChild: NavigationContainer.Key? = null,
    ): ContainerNode = ContainerNode(
        containerKey = key,
        backstack = backstackOf(*backstack),
        children = children,
        activeChild = activeChild,
    )

    private fun shellNode(
        homeBackstack: List<NavigationKey.Instance<*>> = listOf(home),
        predictBackstack: List<NavigationKey.Instance<*>> = listOf(predict),
        activeChild: NavigationContainer.Key? = homeTab,
    ): ContainerNode = node(
        root,
        shell,
        children = listOf(
            node(homeTab, *homeBackstack.toTypedArray()),
            node(predictTab, *predictBackstack.toTypedArray()),
        ),
        activeChild = activeChild,
    )

    @Test
    fun `equality is by instance id, includes the active child, and ignores empty children`() {
        assertEquals(shellNode(), shellNode())
        assertNotEquals(shellNode(activeChild = homeTab), shellNode(activeChild = predictTab))
        assertNotEquals(shellNode(), shellNode(predictBackstack = listOf(predict, round)))

        val withEmptyChild = ContainerNode(
            containerKey = root,
            backstack = backstackOf(shell),
            children = listOf(node(homeTab, home), node(NavigationContainer.Key("empty"))),
            activeChild = homeTab,
        )
        val withoutEmptyChild = ContainerNode(
            containerKey = root,
            backstack = backstackOf(shell),
            children = listOf(node(homeTab, home)),
            activeChild = homeTab,
        )
        assertEquals(withEmptyChild, withoutEmptyChild)
        assertEquals(withEmptyChild.hashCode(), withoutEmptyChild.hashCode())
    }

    @Test
    fun `a longer root backstack is a push`() {
        val previous = node(root, home)
        val current = node(root, home, standings)
        assertEquals(HistoryTransition.Push, classifyTransition(previous, current))
    }

    @Test
    fun `a swapped root backstack is a replace`() {
        val previous = node(root, home)
        val current = node(root, shell)
        assertEquals(HistoryTransition.Replace, classifyTransition(previous, current))
    }

    @Test
    fun `a longer backstack inside a nested container is a push`() {
        val previous = shellNode(activeChild = predictTab)
        val current = shellNode(predictBackstack = listOf(predict, round), activeChild = predictTab)
        assertEquals(HistoryTransition.Push, classifyTransition(previous, current))
    }

    @Test
    fun `a shorter backstack inside a nested container is a replace`() {
        val previous = shellNode(predictBackstack = listOf(predict, round), activeChild = predictTab)
        val current = shellNode(activeChild = predictTab)
        assertEquals(HistoryTransition.Replace, classifyTransition(previous, current))
    }

    @Test
    fun `nested containers appearing for the first time fill in the entry rather than pushing`() {
        val previous = node(root, shell)
        val settled = shellNode(predictBackstack = listOf(predict, round), activeChild = predictTab)
        assertEquals(HistoryTransition.Replace, classifyTransition(previous, settled))

        val partiallyRegistered = node(root, shell, children = listOf(node(homeTab, home)), activeChild = homeTab)
        assertEquals(HistoryTransition.Replace, classifyTransition(partiallyRegistered, shellNode()))
    }

    @Test
    fun `switching between existing containers is a push`() {
        val previous = shellNode(activeChild = homeTab)
        val current = shellNode(activeChild = predictTab)
        assertEquals(HistoryTransition.Push, classifyTransition(previous, current))
    }

    @Test
    fun `the first active container, or one that has just appeared, is a replace`() {
        val noActive = shellNode(activeChild = null)
        assertEquals(HistoryTransition.Replace, classifyTransition(noActive, shellNode(activeChild = homeTab)))

        val onlyHome = node(root, shell, children = listOf(node(homeTab, home)), activeChild = homeTab)
        val predictAppearedAndActive = shellNode(activeChild = predictTab)
        assertEquals(HistoryTransition.Replace, classifyTransition(onlyHome, predictAppearedAndActive))
    }

    @Test
    fun `a nested container that has gone is a replace`() {
        val previous = shellNode()
        val current = node(root, shell, children = listOf(node(homeTab, home)), activeChild = homeTab)
        assertEquals(HistoryTransition.Replace, classifyTransition(previous, current))

        val emptied = node(
            root,
            shell,
            children = listOf(node(homeTab, home), ContainerNode(predictTab, emptyBackstack())),
            activeChild = homeTab,
        )
        assertEquals(HistoryTransition.Replace, classifyTransition(previous, emptied))
    }

    @Test
    fun `a push on the root wins over whatever its old children did`() {
        val previous = shellNode(activeChild = predictTab)
        val current = node(root, shell, standings)
        assertEquals(HistoryTransition.Push, classifyTransition(previous, current))
    }
}
