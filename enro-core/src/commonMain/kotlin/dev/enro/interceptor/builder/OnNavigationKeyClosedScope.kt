package dev.enro.interceptor.builder

import dev.enro.NavigationBackstack
import dev.enro.NavigationKey
import dev.enro.NavigationTransition
import dev.enro.interceptor.NavigationTransitionInterceptor

/**
 * Scope for handling when a navigation key is closed.
 */
public class OnNavigationKeyClosedScope<K : NavigationKey> @PublishedApi internal constructor(
    public val transition: NavigationTransition,
    public val instance: NavigationKey.Instance<K>,
) {
    public val key: K get() = instance.key

    /**
     * Continue with the navigation as normal.
     */
    public fun continueWithClose(): Nothing = throw NavigationTransitionInterceptor.Result.Continue()

    /**
     * Cancel the navigation entirely.
     */
    public fun cancel(): Nothing = throw NavigationTransitionInterceptor.Result.Cancel()

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