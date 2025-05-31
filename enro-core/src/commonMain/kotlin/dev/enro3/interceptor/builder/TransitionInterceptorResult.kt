package dev.enro3.interceptor.builder

import dev.enro3.NavigationBackstack

/**
 * Represents the action to take when intercepting a navigation transition.
 */
@PublishedApi
internal sealed class TransitionInterceptorResult : RuntimeException() {
    /**
     * Continue with the original navigation transition.
     */
    class Continue : TransitionInterceptorResult()

    /**
     * Cancel the navigation transition entirely.
     */
    class Cancel : TransitionInterceptorResult()

    /**
     * Replace the current transition with a modified one.
     */
    class ReplaceWith(
        val backstack: NavigationBackstack
    ) : TransitionInterceptorResult()
}