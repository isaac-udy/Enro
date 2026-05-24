package dev.enro.ui.destinations

import dev.enro.NavigationContainer
import dev.enro.NavigationContext
import dev.enro.NavigationKey

/**
 * Receiver scope of a [SyntheticDestinationScope.sideEffect] block.
 *
 * A side-effect outcome runs deferred — after the dispatcher has rewritten
 * the synthetic's `Open` and any other operations in the same processing
 * pass have settled. That's the point at which `context` and `container`
 * reflect the navigation state the user would expect to see "after" the
 * synthetic ran, and the point at which imperative work (launching a
 * browser intent, rewriting the container's backstack, etc.) is safe.
 *
 * Reaching for a side effect should be deliberate: the pure outcomes
 * (`open`, `close`, `complete`, `completeFrom`) on the parent scope
 * already cover most synthetic patterns. Use a side effect when you need
 * platform handles (e.g. an Android `Activity`), when you need to read or
 * mutate the container's backstack, or when you're bridging to a system
 * outside Enro.
 */
public class SyntheticSideEffectScope<K : NavigationKey> @PublishedApi internal constructor(
    /**
     * The [NavigationContext] from which the synthetic was originally opened.
     * Typically a [dev.enro.context.DestinationContext] (the caller's screen)
     * but may be a [dev.enro.context.ContainerContext] or
     * [dev.enro.context.RootContext] depending on how the synthetic was opened.
     */
    public val context: NavigationContext,
    /**
     * The [NavigationContainer] the synthetic's `Open` was dispatched to.
     * Use `container.execute(context, NavigationOperation.SetBackstack(...))`
     * to rewrite the backstack from inside a side effect.
     */
    public val container: NavigationContainer,
    public val instance: NavigationKey.Instance<K>,
) {
    public val key: K get() = instance.key
}
