package dev.enro.ui.destinations

import dev.enro.NavigationContainer
import dev.enro.NavigationContext
import dev.enro.NavigationKey

/**
 * The receiver of a [SyntheticDestinationScope.sideEffect] block.
 *
 * The side-effect block runs after the container has applied every operation that was executed
 * together with the synthetic's `Open`, so [context] and [container] have the updated
 * navigation state.
 */
public class SyntheticSideEffectScope<K : NavigationKey> @PublishedApi internal constructor(
    /**
     * The [NavigationContext] that opened the synthetic: a
     * [dev.enro.context.DestinationContext], a [dev.enro.context.ContainerContext] or a
     * [dev.enro.context.RootContext].
     */
    public val context: NavigationContext,
    /**
     * The [NavigationContainer] that received the synthetic's `Open`. Call
     * `container.execute(context, NavigationOperation.SetBackstack(...))` to replace the
     * backstack from a side-effect block.
     */
    public val container: NavigationContainer,
    public val instance: NavigationKey.Instance<K>,
) {
    public val key: K get() = instance.key
}
