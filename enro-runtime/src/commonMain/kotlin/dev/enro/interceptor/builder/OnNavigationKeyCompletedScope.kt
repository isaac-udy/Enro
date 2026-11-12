package dev.enro.interceptor.builder

import dev.enro.NavigationBackstack
import dev.enro.NavigationContext
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.context.ContainerContext
import dev.enro.result.NavigationResult
import dev.enro.result.NavigationResultChannel

/**
 * Receiver of an `onCompleted` block. A block that returns without calling [cancel],
 * [cancelAnd], [deliverResultOnly] or [replaceWith] continues the Complete operation.
 */
public class OnNavigationKeyCompletedScope<out K : NavigationKey> @PublishedApi internal constructor(
    public val instance: NavigationKey.Instance<K>,
    internal val data: Any?,
    public val fromContext: NavigationContext,
    public val containerContext: ContainerContext,
) {
    /**
     * The backstack of the container that receives the operation, before the operation
     * is applied.
     */
    public val backstack: NavigationBackstack get() = containerContext.container.backstack

    public val <R : Any> OnNavigationKeyCompletedScope<NavigationKey.WithResult<R>>.result: R get() {
        require(data != null) {
            "Incorrect type, but got null"
        }
        @Suppress("UNCHECKED_CAST")
        return data as R
    }

    /**
     * Ends the block and continues the Complete operation unchanged.
     */
    public fun continueWithComplete(): Nothing =
        throw InterceptorBuilderResult.Continue()

    /**
     * Ends the block and delivers the completed result to the result channel without
     * closing the destination.
     */
    public fun deliverResultOnly(): Nothing {
        cancelAnd {
            NavigationResultChannel.registerResult(
                NavigationResult.Completed(
                    instance,
                    data,
                )
            )
        }
    }

    /**
     * Ends the block and cancels the Complete operation.
     */
    public fun cancel(): Nothing =
        throw InterceptorBuilderResult.Cancel()

    /**
     * Ends the block, cancels the Complete operation and runs [block] as a
     * [NavigationOperation.SideEffect] in place of the Complete operation.
     */
    public fun cancelAnd(block: () -> Unit): Nothing =
        throw InterceptorBuilderResult.CancelAnd(block)

    /**
     * Ends the block and replaces the Complete operation with [operation].
     */
    public fun replaceWith(operation: NavigationOperation): Nothing =
        throw InterceptorBuilderResult.ReplaceWith(operation)

}
