package dev.enro.interceptor.builder

import dev.enro.NavigationBackstack
import dev.enro.NavigationContext
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.context.ContainerContext

/**
 * Scope for handling when a navigation key is closed.
 */
public class OnNavigationKeyClosedScope<K : NavigationKey> @PublishedApi internal constructor(
    public val isSilent: Boolean,
    public val instance: NavigationKey.Instance<K>,
    public val fromContext: NavigationContext,
    public val containerContext: ContainerContext,
) {
    public val key: K get() = instance.key

    /**
     * The current backstack of the container the operation is being applied to.
     * Read this to make decisions based on what's already on the stack — e.g.
     * to find entries above the one being closed and cascade additional closes
     * into an `AggregateOperation`.
     */
    public val backstack: NavigationBackstack get() = containerContext.container.backstack

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
