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
 * One container's backstack, with the containers of the destination at the top of the
 * backstack. The web history plugin writes a [ContainerNode] to `history.state` and restores
 * the containers from a [ContainerNode].
 *
 * [children] are the containers of the top destination, and [activeChild] is the top
 * destination's active container. Equality compares backstacks by instance id, ignores
 * children with an empty backstack, and includes [activeChild], so two nodes that differ only
 * in the active container are not equal.
 *
 * [createNodeFor] reads only the contexts that are registered, so a node that is read while a
 * destination is composing can lack children. [classifyTransition] does not classify the later
 * addition of those children as a push.
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
 * The context of the destination at the top of this container's backstack, or null until that
 * destination has registered a context. The top destination is used, and not the visible
 * destination, because a closed destination stays visible while animating out and must not be
 * recorded.
 */
internal fun ContainerContext.topDestination(): DestinationContext<NavigationKey>? {
    val topId = container.backstack.lastOrNull()?.id ?: return null
    return children.firstOrNull { it.instance.id == topId }
}

/**
 * Returns the node for [container]. With [nestedContainers] false, the node has the
 * container's backstack and no children.
 */
internal fun createNodeFor(
    container: ContainerContext,
    nestedContainers: Boolean = true,
): ContainerNode {
    val top = container.topDestination().takeIf { nestedContainers }
    return ContainerNode(
        containerKey = container.container.key,
        backstack = container.container.backstack,
        children = top?.children.orEmpty().map { createNodeFor(it) },
        activeChild = top?.activeChild?.container?.key,
    )
}

/**
 * Applies [node] to [container]: sets the container's backstack, waits for the top destination
 * to register a context, then applies each child node to the matching container of the top
 * destination. When [node] has children, a container of the top destination that [node] does not
 * list is emptied. Logs a warning and skips the children when the top destination registers no
 * context within the timeout.
 *
 * A backstack with the same instance ids as the node's backstack is not updated, so that the
 * container keeps the existing instances and does not receive deserialized copies.
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

    // The active container is set twice on purpose. Before the children are applied:
    // PreviouslyActiveContainerInterceptor records the active container on each instance that a
    // child opens, and must record the node's active container. After the children are applied:
    // each backstack update makes the updated container active.
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
 * Waits until [createNodeFor] for [container] equals [node]. After [applyNodeFor], a restored
 * destination and the destination's containers register contexts in the compositions that
 * follow the backstack change. Returns false when the nodes are not equal within the timeout.
 */
internal suspend fun awaitNodeFor(
    container: ContainerContext,
    node: ContainerNode,
    nestedContainers: Boolean = true,
): Boolean {
    return withTimeoutOrNull(SETTLE_TIMEOUT_MS) {
        while (createNodeFor(container, nestedContainers) != node) {
            yield()
        }
        true
    } ?: false
}

private const val SETTLE_TIMEOUT_MS = 250L
