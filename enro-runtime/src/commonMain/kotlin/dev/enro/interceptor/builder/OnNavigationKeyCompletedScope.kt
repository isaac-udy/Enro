package dev.enro.interceptor.builder

import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.result.NavigationResult
import dev.enro.result.NavigationResultChannel

/**
 * Scope for handling when a navigation key is completed (either opened or closed).
 */
public class OnNavigationKeyCompletedScope<out K : NavigationKey> @PublishedApi internal constructor(
    public val instance: NavigationKey.Instance<K>,
    internal val data: Any?,
) {
    public val <R : Any> OnNavigationKeyCompletedScope<NavigationKey.WithResult<R>>.result: R get() {
        require(data != null) {
            "Incorrect type, but got null"
        }
        @Suppress("UNCHECKED_CAST")
        return data as R
    }

    /**
     * Continue with the navigation as normal.
     */
    public fun continueWithComplete(): Nothing =
        throw InterceptorBuilderResult.Continue()

    /**
     * Deliver the "complete" result, but don't actually close the screen
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
     * Cancel the navigation entirely.
     */
    public fun cancel(): Nothing =
        throw InterceptorBuilderResult.Cancel()

    /**
     * Cancel the navigation and execute the provided block after the navigation is canceled.
     */
    public fun cancelAnd(block: () -> Unit): Nothing =
        throw InterceptorBuilderResult.CancelAnd(block)

    /**
     * Replace the current operation with a different operation.
     */
    public fun replaceWith(operation: NavigationOperation): Nothing =
        throw InterceptorBuilderResult.ReplaceWith(operation)

}