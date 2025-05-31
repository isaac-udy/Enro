package dev.enro

/**
 * A NavigationTransitionInterceptor is a class that can intercept a navigation transition and either
 * modify the transition, do nothing, or cancel it entirely.
 */
public interface NavigationInterceptor {
    public fun intercept(
        transition: NavigationTransition,
    ): NavigationTransition?
}

