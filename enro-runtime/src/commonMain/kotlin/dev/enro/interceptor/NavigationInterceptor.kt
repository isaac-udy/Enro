package dev.enro.interceptor

import androidx.compose.runtime.Stable
import dev.enro.NavigationContext
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.context.ContainerContext
import dev.enro.result.NavigationResultChannel

/**
 * A NavigationInterceptor is a class that can intercept a navigation transition and
 * return a modified navigation backstack that will be used as the "to" property
 * of the final transition.
 */
@Stable
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
            // Guards against infinite interceptor loops -- chiefly synthetic
            // destinations whose outcomes (directly or transitively) re-open
            // the same synthetic. Real navigation passes rarely exceed a few
            // dozen iterations; 256 leaves room for deep aggregates without
            // letting a runaway loop hang the whole controller.
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
                    // The AggregateOperation branch must come BEFORE the
                    // RootOperation branch -- on K/Native, `is RootOperation`
                    // currently (incorrectly) returns true for
                    // AggregateOperation instances even though
                    // AggregateOperation extends NavigationOperation directly,
                    // not RootOperation. Checking AggregateOperation first
                    // ensures the correct branch runs everywhere.
                    intercepted is NavigationOperation.AggregateOperation -> {
                        // if we get an aggregate operation that contains the SAME operation we started with,
                        // that means we still want to count that operation as being added to the result,
                        // and we don't want to process it again
                        val filtered = intercepted.operations.filter { it != operation }
                        if (filtered.size != intercepted.operations.size) {
                            result.add(operation)
                        }
                        // Different operation returned, add to processing queue
                        toProcess.addAll(0, filtered)
                    }
                    intercepted is NavigationOperation.RootOperation -> {
                        // Different operation returned, add to processing queue
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
                        // We don't filter Close operations whose instance isn't on
                        // the backstack — synthetic destinations legitimately
                        // dispatch a Close for an instance that never landed in
                        // any backstack so their result channel still fires.
                    }
                    is NavigationOperation.Complete<NavigationKey> -> {
                        closedIds.add(it.instance.id)
                        completedResultIds.add(it.instance.metadata.get(NavigationResultChannel.ResultIdKey))
                        // Same reasoning as Close above — synthetics complete
                        // off-backstack instances; the result-channel side
                        // effect is the important bit and must still fire.
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
