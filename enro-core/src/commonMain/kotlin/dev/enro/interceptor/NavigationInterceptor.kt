package dev.enro.interceptor

import dev.enro.NavigationContainer
import dev.enro.NavigationOperation

/**
 * A NavigationInterceptor is a class that can intercept a navigation transition and
 * return a modified navigation backstack that will be used as the "to" property
 * of the final transition.
 */
public interface NavigationInterceptor {
    public fun intercept(
        operation: NavigationOperation,
    ): NavigationOperation?

}
