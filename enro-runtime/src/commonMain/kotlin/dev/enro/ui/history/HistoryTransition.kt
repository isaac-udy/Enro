package dev.enro.ui.history

internal enum class HistoryTransition {
    /** [current] adds to [previous]: a browser entry is pushed. */
    Push,

    /** [current] rewrites [previous]: the browser entry is replaced. */
    Replace,
}

/**
 * How [current] follows [previous], the state recorded in the browser's current entry.
 *
 * Forward is more on top of what was there: a longer backstack in some container, or a different
 * one of a destination's existing containers made active. Removing or swapping entries is a
 * rewrite, since the state it overwrites is no longer reachable in the app and must not survive
 * as a back target. So is a container appearing for the first time: that is a destination still
 * composing, not the user going anywhere, so the entry it belongs to fills in rather than gaining
 * a successor. A container's first backstack can therefore be several entries deep — a seeded
 * deep link — without becoming history of its own.
 */
internal fun classifyTransition(
    previous: ContainerNode,
    current: ContainerNode,
): HistoryTransition {
    return when (extends(previous, current)) {
        true -> HistoryTransition.Push
        else -> HistoryTransition.Replace
    }
}

/**
 * True when [current] adds to [previous]; false when it differs only by containers [previous]
 * had not seen; null when [previous] is not a prefix of it.
 */
private fun extends(
    previous: ContainerNode,
    current: ContainerNode,
): Boolean? {
    if (previous.containerKey != current.containerKey) return null
    val previousIds = previous.backstack.map { it.id }
    val currentIds = current.backstack.map { it.id }
    if (currentIds.take(previousIds.size) != previousIds) return null
    if (currentIds.size > previousIds.size) return true

    val currentChildren = current.children.associateBy { it.containerKey }
    var pushed = false
    for (previousChild in previous.populatedChildren) {
        val currentChild = currentChildren[previousChild.containerKey] ?: return null
        when (extends(previousChild, currentChild)) {
            null -> return null
            true -> pushed = true
            false -> Unit
        }
    }

    val activeChanged = current.activeChild != previous.activeChild &&
        previous.activeChild != null &&
        previous.populatedChildren.any { it.containerKey == current.activeChild }
    return pushed || activeChanged
}
