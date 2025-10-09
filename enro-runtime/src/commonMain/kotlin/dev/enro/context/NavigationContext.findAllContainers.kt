package dev.enro.context

import dev.enro.NavigationContainer

/**
 * Finds all child containers from the given navigation context.
 * This function recursively traverses the navigation hierarchy to find all containers
 * that are descendants of the given context.
 *
 * @param context The navigation context to start searching from
 * @return A list of all child containers found
 *
 * Example usage:
 * ```
 * val rootContext = navigationController.rootContext
 * val allContainers = findAllChildContainers(rootContext)
 *
 * // Find containers from a specific destination context
 * val destinationContext = navigationHandle.context
 * val childContainers = destinationContext.findAllChildContainers()
 * ```
 */
public fun AnyNavigationContext.findAllContainers(): List<NavigationContainer> {
    val containers = mutableListOf<NavigationContainer>()
    fun traverse(currentContext: AnyNavigationContext) {
        when (currentContext) {
            is ContainerContext -> {
                // Add this container
                containers.add(currentContext.container)
            }
            else -> {}
        }
        currentContext.children
            .filterIsInstance<AnyNavigationContext>()
            .forEach { child ->
                traverse(child)
            }
    }
    this.children
        .filterIsInstance<AnyNavigationContext>()
        .forEach { child ->
            traverse(child)
        }
    return containers
}