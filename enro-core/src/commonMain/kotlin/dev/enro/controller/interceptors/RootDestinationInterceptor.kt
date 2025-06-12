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
 * A core navigation interceptor that handles operations targeting root context destinations.
 *
 * This interceptor identifies navigation operations that should open new root contexts (e.g., new activities
 * on Android, new windows on desktop) and redirects them to the appropriate root navigation handle.
 * While most navigation operations resolve to composables that can be rendered within existing containers,
 * some destinations require opening entirely new root contexts.
 *
 * The interceptor works by:
 * 1. Filtering out operations marked as root context destinations
 * 2. Creating a side effect that executes these operations through the root context's navigation handle
 * 3. Allowing platform-specific implementations to handle root context creation appropriately
 *
 * @see NavigationInterceptor
 */
internal object RootDestinationInterceptor : NavigationInterceptor() {
    /**
     * Intercepts navigation operations before they are processed, extracting root context operations
     * and redirecting them to the root navigation handle.
     *
     * @param fromContext The navigation context initiating the operation
     * @param containerContext The container context where operations would normally be rendered
     * @param operations The list of navigation operations to process
     * @return Modified list of operations with root operations replaced by a side effect
     */
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