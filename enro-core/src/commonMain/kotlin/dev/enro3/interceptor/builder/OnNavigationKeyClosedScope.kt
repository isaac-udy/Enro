package dev.enro3.interceptor.builder

import dev.enro3.NavigationBackstack
import dev.enro3.NavigationKey

/**
 * Scope for handling when a navigation key is closed.
 */
public class OnNavigationKeyClosedScope<K : NavigationKey> @PublishedApi internal constructor(
    public val instance: NavigationKey.Instance<K>,
) {
    /**
     * Continue with the navigation as normal.
     */
    public fun continueWithClose(): Nothing = throw TransitionInterceptorResult.Continue()

    /**
     * Cancel the navigation entirely.
     */
    public fun cancel(): Nothing = throw TransitionInterceptorResult.Cancel()

    /**
     * Replace the current transition with a modified one.
     */
    public fun replaceWith(transition: NavigationBackstack): Nothing =
        throw TransitionInterceptorResult.ReplaceWith(transition)
}