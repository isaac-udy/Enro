package dev.enro.core.container

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationContext
import dev.enro.core.getNavigationHandle
import dev.enro.core.parentContainer
import dev.enro.core.readOpenInstruction

internal object DefaultContainerExecutor {
    fun open(
        fromContext: NavigationContext<*>,
        binding: NavigationBinding<*,*>,
        instruction: AnyOpenInstruction,
    ) {
        val container = findContainerFor(fromContext, instruction)

        requireNotNull(container) {
            "Failed to execute instruction from context with NavigationKey ${fromContext.arguments.readOpenInstruction()!!.navigationKey::class.simpleName}: Could not find valid container for NavigationKey of type ${instruction.navigationKey::class.simpleName}"
        }
        container.setBackstack { backstack ->
            backstack.plus(instruction)
        }
    }

    fun close(context: NavigationContext<out Any>) {
        val container = context.parentContainer()
            ?: return

        container.setBackstack {
            it.close(
                context.getNavigationHandle().id
            )
        }
    }

    internal fun findContainerFor(
        fromContext: NavigationContext<*>?,
        instruction: AnyOpenInstruction,
        alreadyVisitedContainer: Set<NavigationContainer> = emptySet()
    ): NavigationContainer? {
        if (fromContext == null) return null
        val containerManager = fromContext.containerManager

        val visited = alreadyVisitedContainer.toMutableSet()
        val container = containerManager
            .getActiveChildContainers(exclude = visited)
            .onEach { visited.add(it) }
            .firstOrNull {
                it.isVisible && it.accept(instruction)
            }
            ?: containerManager.getChildContainers(exclude = visited)
                .onEach { visited.add(it) }
                .filter { it.isVisible }
                .firstOrNull { it.accept(instruction) }

        return container
            ?: findContainerFor(
                fromContext = fromContext.parentContext,
                instruction = instruction,
                alreadyVisitedContainer = visited,
            )
            ?: defaultContainer(fromContext)
    }
}

public expect fun defaultContainer(context: NavigationContext<*>): NavigationContainer?

/**
 * Returns a list of active child containers down from a particular NavigationContainerManager, the results in the list
 * should be in descending distance from the container manager that this was invoked on. This means that the first result will
 * be the active container for this container manager, and the next result will be the active container for that container manager,
 * and so on. This method also takes an "exclude" parameter, which will exclude any containers in the set from the results,
 * including their children.
 */
private fun NavigationContainerManager.getActiveChildContainers(
    exclude: Set<NavigationContainer>,
): List<NavigationContainer> {
    var activeContainer = activeContainer
    val result = mutableListOf<NavigationContainer>()
    while (activeContainer != null) {
        if (exclude.contains(activeContainer)) {
            break
        }
        result.add(activeContainer)
        activeContainer = activeContainer.childContext?.containerManager?.activeContainer
    }
    return result
}

/**
 * Returns a list of all child containers down from a particular NavigationContainerManager, the results in the list
 * should be in descending distance from the container manager that this was invoked on. This is a breadth first search,
 * and doesn't take into account the active container. This method also takes an "exclude" parameter, which will exclude any
 * containers in the exclude set from the results, including the children of containers which are excluded.
 */
private fun NavigationContainerManager.getChildContainers(
    exclude: Set<NavigationContainer>,
): List<NavigationContainer> {
    val toVisit = mutableListOf<NavigationContainer>()
    toVisit.addAll(containers)

    val result = mutableListOf<NavigationContainer>()
    while (toVisit.isNotEmpty()) {
        val next = toVisit.removeAt(0)
        if (exclude.contains(next)) {
            continue
        }
        result.add(next)
        toVisit.addAll(next.childContext?.containerManager?.containers.orEmpty())
    }
    return result
}
