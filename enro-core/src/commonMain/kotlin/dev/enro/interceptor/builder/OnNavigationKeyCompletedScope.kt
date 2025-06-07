package dev.enro.interceptor.builder

import dev.enro.NavigationBackstack
import dev.enro.NavigationKey
import dev.enro.NavigationTransition
import dev.enro.interceptor.NavigationTransitionInterceptor
import dev.enro.result.NavigationResult
import dev.enro.result.NavigationResult.Completed.Companion.result
import dev.enro.result.NavigationResultChannel
import dev.enro.result.clearResult

/**
 * Scope for handling when a navigation key is completed (either opened or closed).
 */
public class OnNavigationKeyCompletedScope<K : NavigationKey> @PublishedApi internal constructor(
    public val transition: NavigationTransition,
    public val instance: NavigationKey.Instance<K>,
    internal val completedResult: NavigationResult.Completed<K>,
) {

    /**
     * Look at the result of the navigation key, without removing or otherwise modifying it.
     */
    public fun <R : Any> OnNavigationKeyCompletedScope<out NavigationKey.WithResult<R>>.peekResult(): R {
        return completedResult.result
    }

    /**
     * Consume the result of the navigation key, removing it from the navigation key instance, so that
     * it cannot be consumed again.
     */
    public fun <R : Any> OnNavigationKeyCompletedScope<out NavigationKey.WithResult<R>>.consumeResult(): R {
        val result = peekResult()
        instance.clearResult()
        return result
    }

    /**
     * Continue with the navigation as normal.
     */
    public fun continueWithComplete(): Nothing =
        throw NavigationTransitionInterceptor.Result.Continue()

    /**
     * Deliver the "complete" result, but don't actually close the screen
     */
    public fun deliverResultOnly(): Nothing {
        NavigationResultChannel.registerResult(instance)
        cancel()
    }

    /**
     * Cancel the navigation entirely.
     */
    public fun cancel(): Nothing =
        throw NavigationTransitionInterceptor.Result.Cancel()

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