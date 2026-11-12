package dev.enro.ui.history

import dev.enro.emptyBackstack

internal enum class HistoryTransition {
    /** [current] adds to [previous]: a browser entry is pushed. */
    Push,

    /** [current] rewrites [previous]: the browser entry is replaced. */
    Replace,
}

/**
 * Classifies the change from [previous], the state recorded in the browser's current entry, to
 * [current].
 *
 * [HistoryTransition.Push]: a container's backstack gained entries on top of the same entries,
 * or a destination made a different one of the destination's populated containers active.
 *
 * [HistoryTransition.Replace]: every other change. Removed or swapped entries are not available
 * in the app any more, so the browser's back button must not return to the previous state. A
 * container that [previous] did not record belongs to a destination that was still composing,
 * so the new container's backstack is added to the current browser entry, however many entries
 * the backstack has.
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
 * True when [current] adds to [previous]. False when [current] equals [previous] or differs
 * only by containers that [previous] did not record. Null when [current] removes or swaps
 * anything that [previous] recorded.
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

/**
 * The id of the closed instance, when the only difference between [current] and [target] is
 * that [target] lacks the top entry of one container. A browser back after a push produces that
 * difference. Null for any other difference.
 */
internal fun closedInstanceId(
    current: ContainerNode,
    target: ContainerNode,
): String? {
    if (current.containerKey != target.containerKey) return null
    val currentIds = current.backstack.map { it.id }
    val targetIds = target.backstack.map { it.id }
    if (currentIds.dropLast(1) == targetIds && currentIds.size == targetIds.size + 1) return currentIds.last()
    if (currentIds != targetIds || current.activeChild != target.activeChild) return null

    val currentChildren = current.populatedChildren.associateBy { it.containerKey }
    val targetChildren = target.populatedChildren.associateBy { it.containerKey }
    if (!currentChildren.keys.containsAll(targetChildren.keys)) return null
    val changed = currentChildren.values.singleOrNull { it != targetChildren[it.containerKey] } ?: return null
    return closedInstanceId(
        current = changed,
        target = targetChildren[changed.containerKey] ?: ContainerNode(changed.containerKey, emptyBackstack()),
    )
}
