package dev.enro.ui.destinations

import dev.enro.NavigationContainer
import dev.enro.NavigationContext
import dev.enro.NavigationKey

/**
 * The decision a synthetic destination's block made when it ran. Returned by
 * [NavigationDestinationProvider.peekSyntheticOutcome] and consumed by
 * `testSyntheticDestination` in enro-test for unit-testing synthetic logic
 * without going through the navigation container's interceptor pipeline.
 *
 * Mirrors the internal `SyntheticDestinationOutcome` sealed class but is a
 * plain value type — not a thrown sentinel — so it can be inspected,
 * pattern-matched and asserted against.
 */
public sealed class SyntheticOutcome {

    /** The synthetic's block called `open(...)` — the dispatcher would rewrite the synthetic's `Open` to `Open(target)`. */
    public data class Open(public val instance: NavigationKey.Instance<*>) : SyntheticOutcome() {
        public val key: NavigationKey get() = instance.key
    }

    /** The synthetic's block called `close()` or `closeSilently()`. */
    public data class Close(public val silent: Boolean) : SyntheticOutcome()

    /** The synthetic's block called `complete(...)`. `result` is the typed payload (or `null` for non-result keys). */
    public data class Complete(public val result: Any?) : SyntheticOutcome()

    /** The synthetic's block called `completeFrom(...)` — the chosen key's eventual completion would route back to the original caller. */
    public data class CompleteFrom(public val instance: NavigationKey.Instance<*>) : SyntheticOutcome() {
        public val key: NavigationKey get() = instance.key
    }

    /**
     * The synthetic's block called `sideEffect { ... }`. The block is not
     * automatically invoked when the outcome is produced — call [runWith]
     * to execute it. Useful in tests that want to inspect *what* side
     * effect the synthetic chose without running it, or that want to
     * provide a controlled scope (e.g. a mock context) for the side effect
     * to operate against.
     */
    public class SideEffect @PublishedApi internal constructor(
        public val instance: NavigationKey.Instance<*>,
        @PublishedApi
        internal val block: SyntheticSideEffectScope<NavigationKey>.() -> Unit,
    ) : SyntheticOutcome() {
        /**
         * Executes the side-effect block with the given [context] and
         * [container] as the scope.
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
