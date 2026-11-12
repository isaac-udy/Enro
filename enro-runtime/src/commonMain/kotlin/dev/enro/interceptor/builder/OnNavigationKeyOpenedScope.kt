package dev.enro.interceptor.builder

import dev.enro.NavigationBackstack
import dev.enro.NavigationContext
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.asInstance
import dev.enro.context.ContainerContext

/**
 * Receiver of an `onOpened` block. A block that returns without calling [cancel], [cancelAnd]
 * or [replaceWith] continues the Open operation.
 */
public class OnNavigationKeyOpenedScope<T : NavigationKey> @PublishedApi internal constructor(
    public val instance: NavigationKey.Instance<T>,
    public val fromContext: NavigationContext,
    public val containerContext: ContainerContext,
) {
    public val key: T get() = instance.key

    /**
     * The backstack of the container that receives the operation, before the operation
     * is applied.
     */
    public val backstack: NavigationBackstack get() = containerContext.container.backstack

    /**
     * Ends the block and continues the Open operation unchanged.
     */
    public fun continueWithOpen(): Nothing = throw InterceptorBuilderResult.Continue()

    /**
     * Ends the block and cancels the Open operation.
     */
    public fun cancel(): Nothing = throw InterceptorBuilderResult.Cancel()

    /**
     * Ends the block, cancels the Open operation and runs [block] as a
     * [NavigationOperation.SideEffect] in place of the Open operation.
     */
    public fun cancelAnd(block: () -> Unit): Nothing = throw InterceptorBuilderResult.CancelAnd(block)

    public fun replaceWith(key: NavigationKey): Nothing =
        replaceWith(instance = key.asInstance())

    public fun replaceWith(key: NavigationKey.WithMetadata<*>): Nothing =
        replaceWith(instance = key.asInstance())

    public fun replaceWith(instance: NavigationKey.Instance<*>): Nothing =
        throw InterceptorBuilderResult.ReplaceWith(NavigationOperation.Open(instance))

    /**
     * Ends the block and replaces the Open operation with [operation], for example a
     * [NavigationOperation.AggregateOperation] or a [NavigationOperation.SetBackstack].
     */
    public fun replaceWith(operation: NavigationOperation): Nothing =
        throw InterceptorBuilderResult.ReplaceWith(operation)
}
