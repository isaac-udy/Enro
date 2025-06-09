package dev.enro.handle

import dev.enro.NavigationContainer
import dev.enro.NavigationOperation
import dev.enro.context.ContainerContext
import dev.enro.context.DestinationContext
import dev.enro.context.NavigationContext
import dev.enro.context.RootContext

internal fun findContainerForOperation(
    fromContext: NavigationContext<*, *>,
    operation: NavigationOperation,
): ContainerContext? {
    return findContainer(
        fromContext = fromContext,
        predicate = { container -> container.accepts(operation) }
    )
}


internal fun findContainer(
    fromContext: NavigationContext<*, *>,
    predicate: (NavigationContainer) -> Boolean,
    alreadyVisitedContainers: Set<NavigationContainer.Key> = emptySet()
): ContainerContext? {
    val visited = alreadyVisitedContainers.toMutableSet()

    // TODO isVisible
    val containerContext = fromContext
        .getActiveChildContainers(exclude = visited)
        .onEach { visited.add(it.container.key) }
        .firstOrNull {
            predicate(it.container)
//            /*it.isVisible &&*/ it.container.accepts(instruction)
        }
        ?: fromContext.getChildContainers(exclude = visited)
            .onEach { visited.add(it.container.key) }
//            .filter { it.isVisible }
            .firstOrNull { predicate(it.container) }

    if (containerContext != null) return containerContext
    val parent = fromContext.parent
    if (parent is NavigationContext<*, *>) {
        return findContainer(
            fromContext = parent,
            predicate = predicate,
            alreadyVisitedContainers = visited,
        )
    }
    return null
}

private fun NavigationContext<*, *>.getActiveChildContainer(): ContainerContext? {
    return when (this) {
        is ContainerContext -> activeChild?.activeChild
        is DestinationContext<*> -> activeChild
        is RootContext -> activeChild
    }
}

private fun NavigationContext<*, *>.getChildContainers(): List<ContainerContext> {
    return when (this) {
        is ContainerContext -> children.flatMap { it.children }
        is DestinationContext<*> -> children
        is RootContext -> children
    }
}

/**
 * Returns a list of active child containers down from a particular NavigationContext, the results in the list
 * should be in descending distance from the context that this was invoked on. This means that the first result will
 * be the active container for this NavigationContext, and the next result will be the active container for that container's context,
 * and so on. This method also takes an "exclude" parameter, which will exclude any containers in the set from the results,
 * including their children.
 */
private fun NavigationContext<*, *>.getActiveChildContainers(
    exclude: Set<NavigationContainer.Key>,
): List<ContainerContext> {
    var activeContainer = getActiveChildContainer()
    val result = mutableListOf<ContainerContext>()
    while (activeContainer != null) {
        if (exclude.contains(activeContainer.container.key)) {
            break
        }
        result.add(activeContainer)
        activeContainer = activeContainer.getActiveChildContainer()
    }
    return result
}

/**
 * Returns a list of all child containers down from a particular NavigationContext, the results in the list
 * should be in descending distance from the context that this was invoked on. This is a breadth first search,
 * and doesn't take into account the active context. This method also takes an "exclude" parameter, which will exclude any
 * containers in the exclude set from the results, including the children of containers which are excluded.
 */
private fun NavigationContext<*, *>.getChildContainers(
    exclude: Set<NavigationContainer.Key>,
): List<ContainerContext> {
    val toVisit = mutableListOf<ContainerContext>()
    toVisit.addAll(getChildContainers())

    val result = mutableListOf<ContainerContext>()
    while (toVisit.isNotEmpty()) {
        val next = toVisit.removeAt(0)
        if (exclude.contains(next.container.key)) {
            continue
        }
        result.add(next)
        toVisit.addAll(next.getChildContainers())
    }
    return result
}