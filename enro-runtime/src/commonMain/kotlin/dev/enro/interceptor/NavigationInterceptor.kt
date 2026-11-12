package dev.enro.interceptor

import androidx.compose.runtime.Stable
import dev.enro.NavigationContext
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.context.ContainerContext
import dev.enro.result.NavigationResultChannel

/**
 * Intercepts the operations of a navigation before a container applies the operations.
 * Each `intercept` function returns the operation unchanged, a replacement operation, or
 * null to cancel the operation.
 */
@Stable
public abstract class NavigationInterceptor {
    // Called with the full list of operations before `intercept` is called for each operation.
    public open fun beforeIntercept(
        fromContext: NavigationContext,
        containerContext: ContainerContext,
        operations: List<NavigationOperation.RootOperation>,
    ) : List<NavigationOperation.RootOperation> {
        return operations
    }

    public open fun intercept(
        fromContext: NavigationContext,
        containerContext: ContainerContext,
        operation: NavigationOperation.Open<NavigationKey>,
    ): NavigationOperation? { return operation }

    public open fun intercept(
        fromContext: NavigationContext,
        containerContext: ContainerContext,
        operation: NavigationOperation.Close<NavigationKey>,
    ): NavigationOperation? { return operation }

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
            // Stops an interceptor loop, such as a synthetic destination whose outcome opens the
            // same synthetic destination again.
            val maxIterations = 256
            var iterations = 0
            while (toProcess.isNotEmpty()) {
                if (++iterations > maxIterations) {
                    error(
                        "Navigation interceptor processing exceeded $maxIterations iterations. " +
                            "This usually means a synthetic destination's outcome opens (directly or " +
                            "transitively) the same synthetic again, or an interceptor is rewriting " +
                            "an operation back to itself. Most recent operation: ${toProcess.first()}"
                    )
                }
                val operation = toProcess.removeAt(0)
                val intercepted = when (operation) {
                    // An Open for an instance that is already in the backstack re-orders the
                    // backstack. The interceptor is not called for a re-order.
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
                        // The interceptor cancelled the operation.
                    }
                    intercepted === operation -> {
                        result.add(operation)
                    }
                    // The AggregateOperation branch must precede the RootOperation branch: on
                    // Kotlin/Native, `is RootOperation` returns true for an AggregateOperation.
                    intercepted is NavigationOperation.AggregateOperation -> {
                        // An aggregate that contains the original operation adds the original
                        // operation to the result. The original operation is not processed again.
                        val filtered = intercepted.operations.filter { it != operation }
                        if (filtered.size != intercepted.operations.size) {
                            result.add(operation)
                        }
                        toProcess.addAll(0, filtered)
                    }
                    intercepted is NavigationOperation.RootOperation -> {
                        toProcess.add(0, intercepted)
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
                        // A Close for an instance outside the backstack is kept: a synthetic
                        // destination is never added to a backstack, and the Close notifies
                        // the result channel of the synthetic destination.
                    }
                    is NavigationOperation.Complete<NavigationKey> -> {
                        closedIds.add(it.instance.id)
                        completedResultIds.add(it.instance.metadata.get(NavigationResultChannel.ResultIdKey))
                        // A Complete for an instance outside the backstack is kept, for the
                        // same reason as a Close.
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

            // Each backstack instance that is not opened, closed or completed by the result is
            // prepended as an Open operation.
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
