package dev.enro3.interceptor.builder

import dev.enro3.NavigationBackstack
import dev.enro3.NavigationTransition
import dev.enro3.interceptor.NavigationTransitionInterceptor

/**
 * Scope for handling any navigation transition.
 */
public class OnNavigationTransitionScope @PublishedApi internal constructor(
    public val transition: NavigationTransition
) {
    /**
     * Continue with the navigation as normal.
     */
    public fun continueWithTransition(): Nothing =
        throw NavigationTransitionInterceptor.Result.Continue()

    /**
     * Cancel the navigation entirely.
     */
    public fun cancel(): Nothing =
        throw NavigationTransitionInterceptor.Result.Cancel()

    /**
     * Replace the current transition with a modified one.
     */
    public fun replaceWith(transition: NavigationBackstack): Nothing =
        throw NavigationTransitionInterceptor.Result.ReplaceWith(transition)
}