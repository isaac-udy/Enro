package dev.enro3.interceptor

import dev.enro3.NavigationOperation

/**
 * A no-op interceptor that does nothing.
 */
public object NoOpNavigationInterceptor : NavigationInterceptor {
    override fun intercept(
        operation: NavigationOperation,
    ): NavigationOperation? {
        return operation
    }
}