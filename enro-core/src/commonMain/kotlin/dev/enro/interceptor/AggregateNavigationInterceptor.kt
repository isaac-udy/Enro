package dev.enro.interceptor

import dev.enro.NavigationBackstack
import dev.enro.NavigationContext
import dev.enro.NavigationKey
import dev.enro.NavigationOperation

internal class AggregateNavigationInterceptor(
    interceptors: List<NavigationInterceptor>,
) : NavigationInterceptor() {
    private val interceptors = interceptors.flatMap { it.flatten() }

    override fun intercept(
        context: NavigationContext,
        operation: NavigationOperation.Open<NavigationKey>,
    ): NavigationOperation? {
        return interceptors.fold(operation) { currentOperation, interceptor ->
            val result = interceptor.intercept(
                context = context,
                operation = currentOperation,
            )
            if (result == null) return null
            if (result !is NavigationOperation.Open<*>) return result
            if (result.instance.id != operation.instance.id) return result
            return@fold result
        }
    }

    override fun intercept(
        context: NavigationContext,
        operation: NavigationOperation.Close<NavigationKey>,
    ): NavigationOperation? {
        return interceptors.fold(operation) { currentOperation, interceptor ->
            val result = interceptor.intercept(
                context = context,
                operation = currentOperation,
            )
            if (result == null) return null
            if (result !is NavigationOperation.Close<*>) return result
            if (result.instance.id != operation.instance.id) return result
            return@fold result
        }
    }

    override fun intercept(
        context: NavigationContext,
        operation: NavigationOperation.Complete<NavigationKey>,
    ): NavigationOperation? {
        return interceptors.fold(operation) { currentOperation, interceptor ->
            val result = interceptor.intercept(
                context = context,
                operation = currentOperation,
            )
            if (result == null) return null
            if (result !is NavigationOperation.Complete<*>) return result
            if (result.instance.id != operation.instance.id) return result
            return@fold result
        }
    }

    override fun beforeIntercept(
        context: NavigationContext,
        backstack: NavigationBackstack,
        operations: List<NavigationOperation.RootOperation>,
    ): List<NavigationOperation.RootOperation> {
        return interceptors.fold(operations) { currentOperations, interceptor ->
            interceptor.beforeIntercept(
                context = context,
                backstack = backstack,
                operations = currentOperations,
            )
        }
    }

    operator fun plus(other: NavigationInterceptor) : AggregateNavigationInterceptor {
        return AggregateNavigationInterceptor(interceptors + other)
    }

    companion object {
        fun NavigationInterceptor.flatten(): List<NavigationInterceptor> {
            return when (this) {
                is AggregateNavigationInterceptor -> interceptors.flatMap { it.flatten() }
                is NoOpNavigationInterceptor -> emptyList()
                else -> listOf(this)
            }
        }
    }
}
