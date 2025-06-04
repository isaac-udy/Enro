package dev.enro.interceptor.builder

import dev.enro.NavigationBackstack
import dev.enro.NavigationTransition
import dev.enro.interceptor.NavigationTransitionInterceptor

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
     * Cancel the navigation and execute the provided block after the navigation is canceled.
     */
    public fun cancelAnd(block: () -> Unit): Nothing =
        throw NavigationTransitionInterceptor.Result.CancelAnd(block)

    /**
     * Replace the current transition with a modified one.
     */
    public fun replaceWith(transition: NavigationBackstack): Nothing =
        throw NavigationTransitionInterceptor.Result.ReplaceWith(transition)
}