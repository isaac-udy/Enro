package dev.enro3.interceptor.builder

import dev.enro3.NavigationBackstack
import dev.enro3.NavigationKey
import dev.enro3.NavigationTransition
import dev.enro3.interceptor.NavigationTransitionInterceptor

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
     * Replace the current transition with a modified one.
     */
    public fun replaceWith(transition: NavigationBackstack): Nothing =
        throw NavigationTransitionInterceptor.Result.ReplaceWith(transition)
}