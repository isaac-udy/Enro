package dev.enro.interceptor

import dev.enro.NavigationContext
import dev.enro.NavigationOperation

/**
 * A no-op interceptor that does nothing.
 */
public object NoOpNavigationInterceptor : NavigationInterceptor {
    override fun intercept(
        context: NavigationContext,
        operation: NavigationOperation,
    ): NavigationOperation? {
        return operation
    }
}