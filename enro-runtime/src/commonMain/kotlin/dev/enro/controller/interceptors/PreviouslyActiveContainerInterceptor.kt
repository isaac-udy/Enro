package dev.enro.controller.interceptors

import dev.enro.NavigationContext
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.context.ContainerContext
import dev.enro.context.DestinationContext
import dev.enro.context.RootContext
import dev.enro.context.activeLeaf
import dev.enro.context.findContext
import dev.enro.context.root
import dev.enro.interceptor.NavigationInterceptor
/**
 * A core Enro interceptor that tracks the active container when navigation operations are executed
 * and restores that container's active state when the navigation is closed.
 * 
 * This interceptor ensures that when a navigation operation opens a destination from a specific
 * container, and that destination is later closed, the original container becomes active again.
 * This is particularly useful in scenarios with multiple containers, such as the HorizontalPager
 * sample in the test application.
 * 
 * The interceptor works by:
 * 1. Attaching metadata to NavigationKey instances during open operations that records which
 *    container was active at the time
 * 2. Reading this metadata during close/complete operations and creating a side effect to
 *    reactivate the previously active container
 * 
 * This functionality is currently enabled by default in Enro but may become optional in future
 * versions.
 */
internal object PreviouslyActiveContainerInterceptor : NavigationInterceptor() {
    override fun beforeIntercept(
        fromContext: NavigationContext,
        containerContext: ContainerContext,
        operations: List<NavigationOperation.RootOperation>,
    ): List<NavigationOperation.RootOperation> {
        if (operations.size != 1) return operations
        val operation = operations.first()
        val previouslyActiveContainer = when (operation) {
            is NavigationOperation.Close<*> -> operation.instance.metadata.get(PreviouslyActiveContainer)
            is NavigationOperation.Complete<*> -> operation.instance.metadata.get(PreviouslyActiveContainer)
            is NavigationOperation.Open<*> -> null
            is NavigationOperation.SideEffect -> null
        } ?: return operations

        if (previouslyActiveContainer == containerContext.id) return operations

        val context = fromContext.root().findContext { it.id == previouslyActiveContainer }
        return operations + NavigationOperation.SideEffect {
            context?.requestActive()
        }
    }

    override fun intercept(
        fromContext: NavigationContext,
        containerContext: ContainerContext,
        operation: NavigationOperation.Open<NavigationKey>,
    ): NavigationOperation? {
        val leaf = fromContext.root().activeLeaf()
        val activeContainerId = when (leaf) {
            is ContainerContext -> leaf.id
            is DestinationContext<*> -> leaf.parent.id
            is RootContext -> return operation
        }
        if (activeContainerId == containerContext.id) return operation
        return operation.apply {
            instance.metadata.set(PreviouslyActiveContainer, activeContainerId)
        }
    }

    private object PreviouslyActiveContainer : NavigationKey.MetadataKey<String?>(null)
}