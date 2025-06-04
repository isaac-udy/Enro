package dev.enro.interceptor

import dev.enro.NavigationOperation

public class AggregateNavigationInterceptor(
    private val interceptors: List<NavigationInterceptor>,
) : NavigationInterceptor {
    override fun intercept(operation: NavigationOperation): NavigationOperation? {
        return interceptors.fold(operation as NavigationOperation?) { currentOperation, interceptor ->
            if (currentOperation == null) return null
            interceptor.intercept(currentOperation)
        }
    }
}
