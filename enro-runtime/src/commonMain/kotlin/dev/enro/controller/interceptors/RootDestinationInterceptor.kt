package dev.enro.controller.interceptors

import dev.enro.NavigationContext
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.context.ContainerContext
import dev.enro.context.root
import dev.enro.interceptor.NavigationInterceptor
import dev.enro.ui.destinations.isRootContextDestination
import dev.enro.viewmodel.getNavigationHandle

/**
 * Removes each Open of a root context destination (an Activity on Android, a window on
 * desktop) from the operations of a container. The removed operations are executed by the
 * NavigationHandle of the root context, in a side effect.
 */
internal object RootDestinationInterceptor : NavigationInterceptor() {
    override fun beforeIntercept(
        fromContext: NavigationContext,
        containerContext: ContainerContext,
        operations: List<NavigationOperation.RootOperation>,
    ): List<NavigationOperation.RootOperation> {
        val rootOperations = operations.filterIsInstance<NavigationOperation.Open<*>>()
            .filter { it.instance.isRootContextDestination(fromContext.controller) }

        val rootOperation = when {
            rootOperations.isEmpty() -> return operations
            rootOperations.size == 1 -> rootOperations.first()
            else -> NavigationOperation.AggregateOperation(rootOperations)
        }
        return (operations - rootOperations).plus(
            NavigationOperation.SideEffect {
                fromContext.root()
                    .getNavigationHandle<NavigationKey>()
                    .execute(rootOperation)
            }
        )
    }
}