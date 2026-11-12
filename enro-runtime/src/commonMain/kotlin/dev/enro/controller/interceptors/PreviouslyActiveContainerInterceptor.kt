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
 * Makes the container that was active before a destination opened active again when the
 * destination is closed or completed.
 *
 * An Open that targets a container other than the active container stores the id of the
 * active container in the metadata of the opened instance. A single Close or Complete of
 * that instance adds a side effect that calls `requestActive` on the stored container.
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

    internal object PreviouslyActiveContainer : NavigationKey.MetadataKey<String?>(
        default = null,
        name = "dev.enro.controller.interceptors.PreviouslyActiveContainerInterceptor.PreviouslyActiveContainer",
    )
}