package dev.enro.interceptor.builder

import dev.enro.NavigationKey
import dev.enro.NavigationOperation

/**
 * Scope for handling when a navigation key is closed.
 */
public class OnNavigationKeyClosedScope<K : NavigationKey> @PublishedApi internal constructor(
    public val instance: NavigationKey.Instance<K>,
) {
    public val key: K get() = instance.key

    /**
     * Continue with the navigation as normal.
     */
    public fun continueWithClose(): Nothing = throw InterceptorBuilderResult.Continue()

    /**
     * Cancel the navigation entirely.
     */
    public fun cancel(): Nothing = throw InterceptorBuilderResult.Cancel()

    /**
     * Cancel the navigation and execute the provided block after the navigation is canceled.
     */
    public fun cancelAnd(block: () -> Unit): Nothing =
        throw InterceptorBuilderResult.CancelAnd(block)

    /**
     * Replace the current transition with a modified one.
     */
    public fun replaceWith(operation: NavigationOperation): Nothing =
        throw InterceptorBuilderResult.ReplaceWith(operation)
}