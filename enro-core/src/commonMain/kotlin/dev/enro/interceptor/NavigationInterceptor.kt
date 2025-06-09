package dev.enro.interceptor

import dev.enro.NavigationContext
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.context.ContainerContext
import dev.enro.platform.EnroLog
import dev.enro.result.NavigationResultChannel

/**
 * A NavigationInterceptor is a class that can intercept a navigation transition and
 * return a modified navigation backstack that will be used as the "to" property
 * of the final transition.
 */
public abstract class NavigationInterceptor {
    // Allows the entire list of operations to be intercepted before
    // any individual operation is intercepted.
    public open fun beforeIntercept(
        fromContext: NavigationContext,
        containerContext: ContainerContext,
        operations: List<NavigationOperation.RootOperation>,
    ) : List<NavigationOperation.RootOperation> {
        return operations
    }

    // Intercept an individual open operation
    public open fun intercept(
        fromContext: NavigationContext,
        containerContext: ContainerContext,
        operation: NavigationOperation.Open<NavigationKey>,
    ): NavigationOperation? { return operation }

    // Intercept an individual close operation
    public open fun intercept(
        fromContext: NavigationContext,
        containerContext: ContainerContext,
        operation: NavigationOperation.Close<NavigationKey>,
    ): NavigationOperation? { return operation }

    // Intercept an individual complete operation
    public open fun intercept(
        fromContext: NavigationContext,
        containerContext: ContainerContext,
        operation: NavigationOperation.Complete<NavigationKey>,
    ): NavigationOperation? { return operation }

    public companion object {
        public fun processOperations(
            fromContext: NavigationContext,
            containerContext: ContainerContext,
            operations: List<NavigationOperation.RootOperation>,
            interceptor: NavigationInterceptor,
        ): List<NavigationOperation.RootOperation> {
            val result = mutableListOf<NavigationOperation.RootOperation>()
            val toProcess = interceptor.beforeIntercept(
                fromContext = fromContext,
                containerContext = containerContext,
                operations = operations,
            ).toMutableList()

            val backstackById = containerContext.container.backstack.associateBy { it.id }
            while (toProcess.isNotEmpty()) {
                val operation = toProcess.removeFirst()
                val intercepted = when (operation) {
                    // If we're getting an Open operation and the backstack already contains
                    // an instance with that id, we skip running the interceptor because this
                    // indicates that the goal of the operation is to re-order the backstack
                    is NavigationOperation.Open<*> -> when {
                        backstackById.containsKey(operation.instance.id) -> operation
                        else -> interceptor.intercept(
                            fromContext = fromContext,
                            containerContext = containerContext,
                            operation = operation,
                        )
                    }
                    is NavigationOperation.Close<*> -> interceptor.intercept(
                        fromContext = fromContext,
                        containerContext = containerContext,
                        operation = operation,
                    )
                    is NavigationOperation.Complete<*> -> interceptor.intercept(
                        fromContext = fromContext,
                        containerContext = containerContext,
                        operation = operation,
                    )
                    else -> operation
                }

                when {
                    intercepted == null -> {
                        // Operation was consumed by interceptor, skip it
                    }
                    intercepted === operation -> {
                        // Same operation returned, add to result
                        result.add(operation)
                    }
                    intercepted is NavigationOperation.RootOperation -> {
                        // Different operation returned, add to processing queue
                        toProcess.add(0, intercepted)
                    }
                    intercepted is NavigationOperation.AggregateOperation -> {
                        // Different operation returned, add to processing queue
                        toProcess.addAll(0, intercepted.operations)
                    }
                }
            }

            val openedIds = mutableSetOf<String>()
            val closedIds = mutableSetOf<String>()
            val completedResultIds = mutableSetOf<NavigationResultChannel.Id?>()
            val filteredResult = result.mapNotNull {
                when (it) {
                    is NavigationOperation.Close<NavigationKey> -> {
                        closedIds.add(it.instance.id)
                        if (!backstackById.containsKey(it.instance.id)) {
                            EnroLog.warn(
                                "Attempted to close a NavigationKey.Instance that was not on the backstack: ${it.instance}."
                            )
                            return@mapNotNull null
                        }
                    }
                    is NavigationOperation.Complete<NavigationKey> -> {
                        closedIds.add(it.instance.id)
                        completedResultIds.add(it.instance.metadata.get(NavigationResultChannel.ResultIdKey))
                        if (!backstackById.containsKey(it.instance.id)) {
                            EnroLog.warn(
                                "Attempted to complete a NavigationKey.Instance that was not on the backstack: ${it.instance}."
                            )
                            return@mapNotNull null
                        }
                    }
                    is NavigationOperation.Open<NavigationKey> -> {
                        openedIds.add(it.instance.id)
                    }
                    is NavigationOperation.SideEffect -> {
                        // No-op
                    }
                }
                return@mapNotNull it
            }

            // Add all non-opened operations as Open operations at the start of the list
            val updatedBackstack = containerContext.container.backstack
                .mapNotNull {
                    val resultId = it.metadata.get(NavigationResultChannel.ResultIdKey)
                    if (resultId != null && completedResultIds.contains(resultId)) {
                        return@mapNotNull null
                    }
                    if (openedIds.contains(it.id)) return@mapNotNull null
                    if (closedIds.contains(it.id)) return@mapNotNull null
                    return@mapNotNull NavigationOperation.Open(it)
                }.plus(filteredResult)

            return updatedBackstack
        }
    }
}
