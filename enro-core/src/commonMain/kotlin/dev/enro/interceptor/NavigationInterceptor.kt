package dev.enro.interceptor

import dev.enro.NavigationContext
import dev.enro.NavigationOperation

/**
 * A NavigationInterceptor is a class that can intercept a navigation transition and
 * return a modified navigation backstack that will be used as the "to" property
 * of the final transition.
 */
public interface NavigationInterceptor {
    public fun intercept(
        context: NavigationContext,
        operation: NavigationOperation,
    ): NavigationOperation?
}
