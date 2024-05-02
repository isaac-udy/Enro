package dev.enro.core.container

import androidx.activity.ComponentActivity
import dev.enro.compatability.Compatibility
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.ExecutorArgs
import dev.enro.core.NavigationContainerKey
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationExecutor
import dev.enro.core.NavigationKey
import dev.enro.core.activity
import dev.enro.core.activity.ActivityNavigationContainer
import dev.enro.core.getNavigationHandle
import dev.enro.core.navigationContext
import dev.enro.core.parentContainer
import dev.enro.core.readOpenInstruction

internal object DefaultContainerExecutor : NavigationExecutor<Any, Any, NavigationKey>(
    fromType = Any::class,
    opensType = Any::class,
    keyType = NavigationKey::class
) {
    override fun open(args: ExecutorArgs<out Any, out Any, out NavigationKey>) {
        if (Compatibility.DefaultContainerExecutor.earlyExitForFragments(args)) return
        if (Compatibility.DefaultContainerExecutor.earlyExitForReplace(args)) return

        val isReplace = args.instruction.navigationDirection == NavigationDirection.Replace
        val fromContext = args.fromContext
        val instruction = Compatibility.DefaultContainerExecutor.getInstructionForCompatibility(args)

        val container = findContainerFor(fromContext, instruction)
        if (
            Compatibility.DefaultContainerExecutor.earlyExitForMissingContainerPush(
                fromContext = fromContext,
                instruction = instruction,
                container = container,
            )
        ) return

        requireNotNull(container) {
            "Failed to execute instruction from context with NavigationKey ${fromContext.arguments.readOpenInstruction()!!.navigationKey::class.java.simpleName}: Could not find valid container for NavigationKey of type ${keyType.java.simpleName}"
        }
        container.setBackstack { backstack ->
            backstack
                .let { if (isReplace) it.pop() else it }
                .plus(instruction)
        }
    }

    override fun close(context: NavigationContext<out Any>) {
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
        alreadyVisitedContainerKeys: Set<String> = emptySet()
    ): NavigationContainer? {
        if (fromContext == null) return null
        if (instruction.navigationDirection == NavigationDirection.ReplaceRoot) {
            return ActivityNavigationContainer(fromContext.activity.navigationContext)
        }
        val containerManager = fromContext.containerManager
        val defaultFragmentContainer = containerManager
            .containers
            .firstOrNull { it.key == NavigationContainerKey.FromId(android.R.id.content) }

        val visited = alreadyVisitedContainerKeys.toMutableSet()
        val container = containerManager
            .getActiveChildContainers(exclude = visited)
            .onEach { visited.add(it.key.name) }
            .firstOrNull {
                it.isVisible && it.accept(instruction) && it != defaultFragmentContainer
            }
            ?: containerManager.getChildContainers(exclude = visited)
                .onEach { visited.add(it.key.name) }
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
            alreadyVisitedContainerKeys = visited,
        )
    }
}

/**
 * Returns a list of active child containers down from a particular NavigationContainerManager, the results in the list
 * should be in descending distance from the container manager that this was invoked on. This means that the first result will
 * be the active container for this container manager, and the next result will be the active container for that container manager,
 * and so on. This method also takes an "exclude" parameter, which will exclude any containers with the given keys from the results,
 * including their children.
 *
 * This will always include the active child of the navigation container that this was invoked on, regardless of whether the
 * key of that container is in the exclude set or not.
 */
private fun NavigationContainerManager.getActiveChildContainers(
    exclude: Set<String>,
): List<NavigationContainer> {
    val result = mutableListOf<NavigationContainer>()
    activeContainer?.let { result.add(it) }

    var activeContainer = activeContainer?.childContext?.containerManager?.activeContainer
    while (activeContainer != null) {
        result.add(activeContainer)
        if (exclude.contains(activeContainer.key.name)) {
            break
        }
        activeContainer = activeContainer.childContext?.containerManager?.activeContainer
    }
    return result
}

/**
 * Returns a list of all child containers down from a particular NavigationContainerManager, the results in the list
 * should be in descending distance from the container manager that this was invoked on. This is a breadth first search,
 * and doesn't take into account the active container. This method also takes an "exclude" parameter, which will exclude any
 * results with the given keys from the results, including the children of containers which are excluded.
 */
private fun NavigationContainerManager.getChildContainers(
    exclude: Set<String>,
): List<NavigationContainer> {
    val toVisit = mutableListOf<NavigationContainer>()
    toVisit.addAll(containers)

    val result = mutableListOf<NavigationContainer>()
    while (toVisit.isNotEmpty()) {
        val next = toVisit.removeAt(0)
        if (exclude.contains(next.key.name)) {
            continue
        }
        result.add(next)
        toVisit.addAll(next.childContext?.containerManager?.containers.orEmpty())
    }
    return result
}
