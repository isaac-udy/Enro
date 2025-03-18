package dev.enro.core.container

import androidx.activity.ComponentActivity
import dev.enro.compatability.Compatibility
import dev.enro.core.*
import dev.enro.core.activity.ActivityNavigationContainer

internal object DefaultContainerExecutor {
    fun open(
        fromContext: NavigationContext<*>,
        binding: NavigationBinding<*,*>,
        instruction: AnyOpenInstruction,
    ) {
        if (Compatibility.DefaultContainerExecutor.earlyExitForFragments(fromContext)) return
        if (
            Compatibility.DefaultContainerExecutor.earlyExitForReplace(
                fromContext = fromContext,
                instruction = instruction,
            )
        ) return

        val isReplace = instruction.navigationDirection == NavigationDirection.Replace
        val instruction = Compatibility.DefaultContainerExecutor.getInstructionForCompatibility(
            binding = binding,
            fromContext = fromContext,
            instruction = instruction,
        )

        val container = findContainerFor(fromContext, instruction)
        if (
            Compatibility.DefaultContainerExecutor.earlyExitForMissingContainerPush(
                fromContext = fromContext,
                instruction = instruction,
                container = container,
            )
        ) return

        requireNotNull(container) {
            "Failed to execute instruction from context with NavigationKey ${fromContext.arguments.readOpenInstruction()!!.navigationKey::class.simpleName}: Could not find valid container for NavigationKey of type ${instruction.navigationKey::class.simpleName}"
        }
        container.setBackstack { backstack ->
            backstack
                .let { if (isReplace) it.pop() else it }
                .plus(instruction)
        }
    }

    fun close(context: NavigationContext<out Any>) {
        if (Compatibility.DefaultContainerExecutor.earlyExitForNoContainer(context)) return

        val container = context.parentContainer()
            ?: (context.contextReference as? ComponentActivity)?.let {
                ActivityNavigationContainer(context as NavigationContext<ComponentActivity>)
            }
            ?: return

        container.setBackstack {
            it.close(
                context.getNavigationHandle().id
            )
        }
    }

    private fun findContainerFor(
        fromContext: NavigationContext<*>?,
        instruction: AnyOpenInstruction,
        alreadyVisitedContainer: Set<NavigationContainer> = emptySet()
    ): NavigationContainer? {
        if (fromContext == null) return null
        if (instruction.navigationDirection == NavigationDirection.ReplaceRoot) {
            return ActivityNavigationContainer(fromContext.activity.navigationContext)
        }
        val containerManager = fromContext.containerManager
        val defaultFragmentContainer = containerManager
            .containers
            .firstOrNull { it.key == NavigationContainerKey.FromId(android.R.id.content) }

        val visited = alreadyVisitedContainer.toMutableSet()
        val container = containerManager
            .getActiveChildContainers(exclude = visited)
            .onEach { visited.add(it) }
            .firstOrNull {
                it.isVisible && it.accept(instruction) && it != defaultFragmentContainer
            }
            ?: containerManager.getChildContainers(exclude = visited)
                .onEach { visited.add(it) }
                .filter { it.isVisible }
                .filterNot { it == defaultFragmentContainer }
                .firstOrNull { it.accept(instruction) }
                .let {
                    val useDefaultFragmentContainer = it == null &&
                            fromContext.parentContext == null &&
                            defaultFragmentContainer != null &&
                            defaultFragmentContainer.accept(instruction)

                    val useActivityContainer = it == null &&
                            fromContext.parentContext == null &&
                            instruction.navigationDirection != NavigationDirection.Push

                    when {
                        useDefaultFragmentContainer -> defaultFragmentContainer
                        useActivityContainer -> ActivityNavigationContainer(fromContext.activity.navigationContext)
                        else -> it
                    }
                }

        return container ?: findContainerFor(
            fromContext = fromContext.parentContext,
            instruction = instruction,
            alreadyVisitedContainer = visited,
        )
    }
}

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
