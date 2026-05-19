package dev.enro.interceptor.builder

import dev.enro.NavigationBackstack
import dev.enro.NavigationContext
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.asInstance
import dev.enro.context.ContainerContext

/**
 * Scope for handling when a navigation key is opened.
 */
public class OnNavigationKeyOpenedScope<T : NavigationKey> @PublishedApi internal constructor(
    public val instance: NavigationKey.Instance<T>,
    public val fromContext: NavigationContext,
    public val containerContext: ContainerContext,
) {
    public val key: T get() = instance.key

    /**
     * The current backstack of the container the operation is being applied to.
     * Read this to make decisions based on what's already on the stack — e.g.
     * to find an anchor entry and rewrite the operation into a `SetBackstack`
     * or an `AggregateOperation`.
     */
    public val backstack: NavigationBackstack get() = containerContext.container.backstack

    /**
     * Continue with the navigation as normal.
     */
    public fun continueWithOpen(): Nothing = throw InterceptorBuilderResult.Continue()

    /**
     * Cancel the navigation entirely.
     */
    public fun cancel(): Nothing = throw InterceptorBuilderResult.Cancel()

    /**
     * Cancel the navigation and execute the provided block after the navigation is canceled.
     */
    public fun cancelAnd(block: () -> Unit): Nothing = throw InterceptorBuilderResult.CancelAnd(block)

    public fun replaceWith(key: NavigationKey): Nothing =
        replaceWith(instance = key.asInstance())

    public fun replaceWith(key: NavigationKey.WithMetadata<*>): Nothing =
        replaceWith(instance = key.asInstance())

    public fun replaceWith(instance: NavigationKey.Instance<*>): Nothing =
        throw InterceptorBuilderResult.ReplaceWith(NavigationOperation.Open(instance))

    /**
     * Replace this Open with an arbitrary [NavigationOperation] — for example
     * an [NavigationOperation.AggregateOperation] that closes some existing
     * entries before opening the new one, or a `SetBackstack` transition.
     */
    public fun replaceWith(operation: NavigationOperation): Nothing =
        throw InterceptorBuilderResult.ReplaceWith(operation)
}
