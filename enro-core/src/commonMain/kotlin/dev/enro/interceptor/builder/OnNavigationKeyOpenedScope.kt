package dev.enro.interceptor.builder

import dev.enro.NavigationBackstack
import dev.enro.NavigationKey
import dev.enro.NavigationTransition
import dev.enro.asInstance
import dev.enro.interceptor.NavigationTransitionInterceptor

/**
 * Scope for handling when a navigation key is opened.
 */
public class OnNavigationKeyOpenedScope<T : NavigationKey>(
    internal val transition: NavigationTransition,
    public val instance: NavigationKey.Instance<T>,
) {
    public val key: T get() = instance.key

    /**
     * Continue with the navigation as normal.
     */
    public fun continueWithOpen(): Nothing = throw NavigationTransitionInterceptor.Result.Continue()

    /**
     * Cancel the navigation entirely.
     */
    public fun cancel(): Nothing = throw NavigationTransitionInterceptor.Result.Cancel()

    /**
     * Cancel the navigation and execute the provided block after the navigation is canceled.
     */
    public fun cancelAnd(block: () -> Unit): Nothing = throw NavigationTransitionInterceptor.Result.CancelAnd(block)

    /**
     * Replace the current transition with a modified one.
     */
    public fun replaceWith(backstack: NavigationBackstack): Nothing =
        throw NavigationTransitionInterceptor.Result.ReplaceWith(backstack)

    public fun replaceWith(key: NavigationKey): Nothing =
        replaceWith(instance = key.asInstance())

    public fun replaceWith(key: NavigationKey.WithMetadata<*>): Nothing =
        replaceWith(instance = key.asInstance())

    public fun replaceWith(instance: NavigationKey.Instance<*>): Nothing =
        throw NavigationTransitionInterceptor.Result.ReplaceWith(
            backstack = transition.targetBackstack.map {
                if (it == this.instance) instance else it
            }
        )
}