package dev.enro.interceptor

import dev.enro.NavigationContext
import dev.enro.NavigationKey
import dev.enro.NavigationOperation

/**
 * A NavigationInterceptor is a class that can intercept a navigation transition and
 * return a modified navigation backstack that will be used as the "to" property
 * of the final transition.
 */
public abstract class NavigationInterceptor {
    public open fun intercept(
        context: NavigationContext,
        operation: NavigationOperation.Open<NavigationKey>,
    ): NavigationOperation? { return operation }

    public open fun intercept(
        context: NavigationContext,
        operation: NavigationOperation.Close<NavigationKey>,
    ): NavigationOperation? { return operation }

    public open fun intercept(
        context: NavigationContext,
        operation: NavigationOperation.Complete<NavigationKey>,
    ): NavigationOperation? { return operation }

    public companion object {
        public fun processOperations(
            context: NavigationContext,
            operations: List<NavigationOperation.RootOperation>,
            interceptor: NavigationInterceptor,
        ): List<NavigationOperation.RootOperation> {
            val result = mutableListOf<NavigationOperation.RootOperation>()
            val toProcess = operations.toMutableList()

            while (toProcess.isNotEmpty()) {
                val operation = toProcess.removeFirst()
                val intercepted = when (operation) {
                    is NavigationOperation.Open<*> -> interceptor.intercept(context, operation)
                    is NavigationOperation.Close<*> -> interceptor.intercept(context, operation)
                    is NavigationOperation.Complete<*> -> interceptor.intercept(context, operation)
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

            return result
        }
    }
}
