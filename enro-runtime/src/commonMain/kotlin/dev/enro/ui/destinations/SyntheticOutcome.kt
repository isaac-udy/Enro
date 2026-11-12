package dev.enro.ui.destinations

import dev.enro.NavigationContainer
import dev.enro.NavigationContext
import dev.enro.NavigationKey

/**
 * The outcome that a synthetic destination's block selected. Returned by `peekSyntheticOutcome`,
 * which runs the block without executing a navigation operation. `testSyntheticDestination` in
 * enro-test asserts on a [SyntheticOutcome].
 */
public sealed class SyntheticOutcome {

    /** The synthetic's block called `open(...)` with [instance]. */
    public data class Open(public val instance: NavigationKey.Instance<*>) : SyntheticOutcome() {
        public val key: NavigationKey get() = instance.key
    }

    /**
     * The synthetic's block called `close()` or `closeSilently()`, or returned without calling
     * an outcome method. [silent] is `false` only for `close()`.
     */
    public data class Close(public val silent: Boolean) : SyntheticOutcome()

    /** The synthetic's block called `complete(...)`. [result] is `null` for `complete()`. */
    public data class Complete(public val result: Any?) : SyntheticOutcome()

    /** The synthetic's block called `completeFrom(...)` with [instance]. */
    public data class CompleteFrom(public val instance: NavigationKey.Instance<*>) : SyntheticOutcome() {
        public val key: NavigationKey get() = instance.key
    }

    /**
     * The synthetic's block called `sideEffect { ... }`. The side-effect block has not run; call
     * [runWith] to run the side-effect block.
     */
    public class SideEffect @PublishedApi internal constructor(
        public val instance: NavigationKey.Instance<*>,
        @PublishedApi
        internal val block: SyntheticSideEffectScope<NavigationKey>.() -> Unit,
    ) : SyntheticOutcome() {
        /**
         * Runs the side-effect block with a [SyntheticSideEffectScope] built from [context] and
         * [container].
         */
        public fun runWith(
            context: NavigationContext,
            container: NavigationContainer,
        ) {
            @Suppress("UNCHECKED_CAST")
            val scope = SyntheticSideEffectScope(
                context = context,
                container = container,
                instance = instance as NavigationKey.Instance<NavigationKey>,
            )
            scope.block()
        }
    }
}
