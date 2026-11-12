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
        predicate = { container -> container.accepts(fromContext, operation) }
    )
}


internal fun findContainer(
    fromContext: NavigationContext<*, *>,
    predicate: (NavigationContainer) -> Boolean,
    alreadyVisitedContainers: Set<NavigationContainer.Key> = emptySet()
): ContainerContext? {
    val visited = alreadyVisitedContainers.toMutableSet()
    // TODO: skip a container that is not visible.
    val containerContext = fromContext
        .getActiveChildContainers(exclude = visited)
        .onEach { visited.add(it.container.key) }
        .firstOrNull {
            predicate(it.container)
        }
        ?: fromContext.getChildContainers(exclude = visited)
            .onEach { visited.add(it.container.key) }
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
        // A destination removed from the backstack stays registered while the destination
        // animates out. The containers of that destination must not receive new operations.
        is ContainerContext -> {
            val backstackIds = container.backstack.map { it.id }.toSet()
            children
                .filter { it.id in backstackIds }
                .flatMap { it.children }
        }
        is DestinationContext<*> -> children
        is RootContext -> children
    }
}

// Returns the chain of active child containers below this context, nearest first. The chain
// ends before the first container in `exclude`.
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

// Returns each container below this context in breadth-first order, active or not. A container
// in `exclude` is left out together with the containers below that container.
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