package dev.enro.interceptor.builder

import dev.enro.NavigationBackstack
import dev.enro.NavigationContext
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.context.ContainerContext

/**
 * Receiver of an `onClosed` block. A block that returns without calling [cancel], [cancelAnd]
 * or [replaceWith] continues the Close operation.
 */
public class OnNavigationKeyClosedScope<K : NavigationKey> @PublishedApi internal constructor(
    public val isSilent: Boolean,
    public val instance: NavigationKey.Instance<K>,
    public val fromContext: NavigationContext,
    public val containerContext: ContainerContext,
) {
    public val key: K get() = instance.key

    /**
     * The backstack of the container that receives the operation, before the operation
     * is applied.
     */
    public val backstack: NavigationBackstack get() = containerContext.container.backstack

    /**
     * Ends the block and continues the Close operation unchanged.
     */
    public fun continueWithClose(): Nothing = throw InterceptorBuilderResult.Continue()

    /**
     * Ends the block and cancels the Close operation.
     */
    public fun cancel(): Nothing = throw InterceptorBuilderResult.Cancel()

    /**
     * Ends the block, cancels the Close operation and runs [block] as a
     * [NavigationOperation.SideEffect] in place of the Close operation.
     */
    public fun cancelAnd(block: () -> Unit): Nothing =
        throw InterceptorBuilderResult.CancelAnd(block)

    /**
     * Ends the block and replaces the Close operation with [operation].
     */
    public fun replaceWith(operation: NavigationOperation): Nothing =
        throw InterceptorBuilderResult.ReplaceWith(operation)
}
