package dev.enro.interceptor

import dev.enro.NavigationContext
import dev.enro.NavigationOperation

internal class AggregateNavigationInterceptor(
    interceptors: List<NavigationInterceptor>,
) : NavigationInterceptor {
    private val interceptors = interceptors.flatMap { it.flatten() }

    override fun intercept(
        context: NavigationContext,
        operation: NavigationOperation,
    ): NavigationOperation? {
        return interceptors.fold(operation as NavigationOperation?) { currentOperation, interceptor ->
            if (currentOperation == null) return null
            interceptor.intercept(
                context,
                currentOperation,
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
