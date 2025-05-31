package dev.enro3.interceptor.builder

import dev.enro3.NavigationBackstack
import dev.enro3.NavigationKey
import dev.enro3.NavigationTransition
import dev.enro3.asInstance

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
    public fun continueWithOpen(): Nothing = throw TransitionInterceptorResult.Continue()

    /**
     * Cancel the navigation entirely.
     */
    public fun cancel(): Nothing = throw TransitionInterceptorResult.Cancel()

    /**
     * Replace the current transition with a modified one.
     */
    public fun replaceWith(backstack: NavigationBackstack): Nothing =
        throw TransitionInterceptorResult.ReplaceWith(backstack)

    public fun replaceWith(key: NavigationKey): Nothing =
        replaceWith(instance = key.asInstance())

    public fun replaceWith(key: NavigationKey.WithMetadata<*>): Nothing =
        replaceWith(instance = key.asInstance())

    public fun replaceWith(instance: NavigationKey.Instance<*>): Nothing =
        throw TransitionInterceptorResult.ReplaceWith(
            backstack = transition.targetBackstack.map {
                if (it == this.instance) instance else it
            }
        )
}