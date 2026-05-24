package dev.enro.ui.destinations

import dev.enro.NavigationKey

/**
 * Sentinel exception thrown by the outcome methods on [SyntheticDestinationScope]
 * (e.g. `open`, `close`, `closeSilently`, `complete`, `completeFrom`, `sideEffect`)
 * to signal the synthetic's decision back up to the synthetic dispatcher. The
 * dispatcher catches the subclass and converts it to the corresponding
 * [dev.enro.NavigationOperation].
 *
 * Outcomes split into two kinds:
 *
 *  - **Pure outcomes** ([Open], [Close], [Complete], [CompleteFrom]) become an
 *    in-place rewrite of the original `Open(synthetic)` — the dispatcher returns
 *    the equivalent operation from inside its interceptor, so the rewrite is
 *    applied during the same `processOperations` pass. Ordering is preserved.
 *  - **The side-effect outcome** ([SideEffect]) is dispatched as a
 *    `NavigationOperation.SideEffect`, which runs in `afterExecution` once
 *    every other operation in the current pass has settled. Used when the
 *    synthetic needs platform context, the container reference, or arbitrary
 *    imperative work.
 *
 * Throwing for control flow mirrors the pattern used by
 * [InterceptorBuilderResult][dev.enro.interceptor.builder.InterceptorBuilderResult]
 * — it lets the scope methods return [Nothing] so the synthetic block can
 * short-circuit naturally from inside conditionals.
 */
internal sealed class SyntheticDestinationOutcome : RuntimeException() {

    internal class Open(
        val target: NavigationKey.Instance<NavigationKey>,
    ) : SyntheticDestinationOutcome()

    /** Regular close fires the result-channel callback; silent close does not. */
    internal class Close(
        val silent: Boolean,
    ) : SyntheticDestinationOutcome()

    internal class Complete(
        val result: Any?,
    ) : SyntheticDestinationOutcome()

    internal class CompleteFrom(
        val target: NavigationKey.Instance<NavigationKey>,
    ) : SyntheticDestinationOutcome()

    /**
     * The block runs deferred, in `afterExecution` of the operation processing
     * pass that intercepted the synthetic. By that point any other operations
     * in the same pass have settled, so the side effect sees the post-rewrite
     * backstack state. The dispatcher wraps this in a
     * [dev.enro.NavigationOperation.SideEffect] and constructs the
     * [SyntheticSideEffectScope] receiver when the side effect actually runs.
     */
    internal class SideEffect(
        val block: SyntheticSideEffectScope<NavigationKey>.() -> Unit,
    ) : SyntheticDestinationOutcome()
}
