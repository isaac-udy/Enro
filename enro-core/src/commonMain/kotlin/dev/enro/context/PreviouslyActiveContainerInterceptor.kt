package dev.enro.context

import dev.enro.NavigationContext
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.interceptor.NavigationInterceptor
import dev.enro.platform.EnroLog

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
        EnroLog.error("Setting active container id: $activeContainerId")
        EnroLog.error(fromContext.root().getDebugString())
        return operation.apply {
            instance.metadata.set(PreviouslyActiveContainer, activeContainerId)
        }
    }

    private object PreviouslyActiveContainer : NavigationKey.MetadataKey<String?>(null)
}