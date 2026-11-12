package dev.enro.context

import dev.enro.NavigationContainer

/**
 * Returns the [NavigationContainer] of each [ContainerContext] below this context, in
 * depth-first order. The result does not include the container of this context.
 */
public fun AnyNavigationContext.findAllContainers(): List<NavigationContainer> {
    val containers = mutableListOf<NavigationContainer>()
    fun traverse(currentContext: AnyNavigationContext) {
        when (currentContext) {
            is ContainerContext -> {
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