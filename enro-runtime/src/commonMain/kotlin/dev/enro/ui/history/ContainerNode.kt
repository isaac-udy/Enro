package dev.enro.ui.history

import dev.enro.NavigationBackstack
import dev.enro.NavigationContainer
import dev.enro.NavigationKey
import dev.enro.context.ContainerContext
import dev.enro.context.DestinationContext
import dev.enro.emptyBackstack
import dev.enro.platform.EnroLog
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.yield
import kotlinx.serialization.Serializable

/**
 * One container's backstack together with the containers nested under the destination at the
 * top of it: the unit the web history plugin writes to `history.state` and restores from it.
 *
 * [children] are the containers that destination hosts, each a subtree of its own, and
 * [activeChild] is the one it has active, which for a tab shell is the selected tab. Equality
 * is by instance id, ignores children with nothing in them, and includes [activeChild], so a
 * tab switch is a different state from the one before it.
 *
 * A node is read from whichever contexts have registered, so one read while a destination is
 * still composing can lack children the settled tree has. The plugin never treats that growth
 * as navigation — see [classifyTransition].
 */
@Serializable
internal data class ContainerNode(
    val containerKey: NavigationContainer.Key,
    val backstack: NavigationBackstack,
    val children: List<ContainerNode> = emptyList(),
    val activeChild: NavigationContainer.Key? = null,
) {
    val populatedChildren: List<ContainerNode>
        get() = children.filter { it.backstack.isNotEmpty() }.sortedBy { it.containerKey.name }

    override fun toString(): String {
        val content = "backstack = [${backstack.joinToString { it.key.toString() }}],\n" +
            "activeChild = ${activeChild?.name},\n" +
            "children = [${children.joinToString { it.toString() }}],\n"
        return buildString {
            appendLine("ContainerNode(")
            content.lines().forEach {
                appendLine(it.prependIndent("    "))
            }
            append(")")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (other::class != this::class) return false

        other as ContainerNode

        if (containerKey != other.containerKey) return false
        if (backstack.map { it.id } != other.backstack.map { it.id }) return false
        if (activeChild != other.activeChild) return false
        return populatedChildren == other.populatedChildren
    }

    override fun hashCode(): Int {
        var result = containerKey.hashCode()
        result = 31 * result + backstack.map { it.id }.hashCode()
        result = 31 * result + activeChild.hashCode()
        result = 31 * result + populatedChildren.hashCode()
        return result
    }
}

/**
 * The context of the destination at the top of this container's backstack, once it has composed
 * and registered. The top entry rather than the visible one: while a close animates out, the
 * closed destination is still visible but no longer part of the state worth recording.
 */
internal fun ContainerContext.topDestination(): DestinationContext<NavigationKey>? {
    val topId = container.backstack.lastOrNull()?.id ?: return null
    return children.firstOrNull { it.instance.id == topId }
}

internal fun createNodeFor(
    container: ContainerContext,
): ContainerNode {
    val top = container.topDestination()
    return ContainerNode(
        containerKey = container.container.key,
        backstack = container.container.backstack,
        children = top?.children.orEmpty().map { createNodeFor(it) },
        activeChild = top?.activeChild?.container?.key,
    )
}

/**
 * Drives the tree under [container] to [node]: this container's backstack first, then, once the
 * destination that puts on top has registered its context, each of its containers in turn.
 * Containers the node does not list are emptied, since the node is the whole of what was
 * recorded, and the destination's active container is set last, after the containers it may
 * name exist.
 *
 * Backstacks are compared by instance id, so a container already holding the recorded entries
 * is left alone rather than handed deserialized copies of instances it already has.
 */
internal suspend fun applyNodeFor(
    container: ContainerContext,
    node: ContainerNode,
) {
    if (container.container.backstack.map { it.id } != node.backstack.map { it.id }) {
        container.container.updateBackstack(container) { node.backstack }
    }
    if (node.children.isEmpty()) return
    val top = withTimeoutOrNull(SETTLE_TIMEOUT_MS) {
        while (container.topDestination() == null) {
            yield()
        }
        container.topDestination()
    }
    if (top == null) {
        EnroLog.warn("WebHistoryPlugin: failed to restore child containers while applying popped state")
        return
    }
    val containers = top.children
        .associateBy { it.container.key }
        .toMutableMap()

    // The active container is set before the children are applied and again after. Before: an
    // entry a child gains is an Open, and PreviouslyActiveContainerInterceptor records on it
    // whichever container is active at that moment, to reactivate on close — with the recorded
    // container active, nothing stale is recorded. After: every backstack change makes its own
    // container active, so the last child applied would otherwise be the one left active.
    node.activeChild?.let { top.setActiveContainer(it.name) }
    node.children.forEach { childNode ->
        val child = containers.remove(childNode.containerKey)
        if (child != null) {
            applyNodeFor(child, childNode)
        }
    }
    containers.forEach { (_, child) ->
        child.container.updateBackstack(child) { emptyBackstack() }
    }
    node.activeChild?.let { top.setActiveContainer(it.name) }
}

/**
 * Waits for the tree under [container] to read as [node], which after [applyNodeFor] takes a
 * frame or two: a restored destination registers its context, and its containers theirs, on the
 * composition that follows the backstack change. False when it has not settled in time.
 */
internal suspend fun awaitNodeFor(
    container: ContainerContext,
    node: ContainerNode,
): Boolean {
    return withTimeoutOrNull(SETTLE_TIMEOUT_MS) {
        while (createNodeFor(container) != node) {
            yield()
        }
        true
    } ?: false
}

private const val SETTLE_TIMEOUT_MS = 250L
