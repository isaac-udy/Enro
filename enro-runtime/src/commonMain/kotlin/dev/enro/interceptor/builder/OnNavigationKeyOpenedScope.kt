package dev.enro.interceptor.builder

import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.asInstance

/**
 * Scope for handling when a navigation key is opened.
 */
public class OnNavigationKeyOpenedScope<T : NavigationKey>(
    public val instance: NavigationKey.Instance<T>,
) {
    public val key: T get() = instance.key

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
}