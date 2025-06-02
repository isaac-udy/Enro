package dev.enro3.interceptor.builder

import dev.enro3.NavigationBackstack
import dev.enro3.NavigationKey
import dev.enro3.NavigationTransition
import dev.enro3.interceptor.NavigationTransitionInterceptor
import dev.enro3.result.NavigationResult
import dev.enro3.result.NavigationResult.Completed.Companion.result
import dev.enro3.result.NavigationResultChannel

/**
 * Scope for handling when a navigation key is completed (either opened or closed).
 */
public class OnNavigationKeyCompletedScope<K : NavigationKey> @PublishedApi internal constructor(
    public val transition: NavigationTransition,
    public val instance: NavigationKey.Instance<K>,
    internal val completedResult: NavigationResult.Completed<K>,
) {

    public val <R : Any> OnNavigationKeyCompletedScope<out NavigationKey.WithResult<R>>.result: R
        get() {
            return completedResult.result
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
     * Replace the current transition with a modified one.
     */
    public fun replaceWith(transition: NavigationBackstack): Nothing =
        throw NavigationTransitionInterceptor.Result.ReplaceWith(transition)

}