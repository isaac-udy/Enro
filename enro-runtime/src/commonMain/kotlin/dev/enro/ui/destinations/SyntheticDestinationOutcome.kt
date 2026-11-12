package dev.enro.ui.destinations

import dev.enro.NavigationKey

/**
 * Thrown by the outcome methods on [SyntheticDestinationScope], so that each outcome method
 * returns [Nothing] and ends the synthetic's block. The interceptor in `SyntheticDestination`
 * catches the outcome and returns a [dev.enro.NavigationOperation] in place of the original
 * `Open(synthetic)`, at the same position in the list of operations:
 *
 *  - [Open], [Close], [Complete] and [CompleteFrom] become the matching operation.
 *  - [SideEffect] becomes a `NavigationOperation.SideEffect`, which the container runs after
 *    the container has applied every operation that was executed together with the `Open`.
 *
 * [InterceptorBuilderResult][dev.enro.interceptor.builder.InterceptorBuilderResult] uses the
 * same throw-for-control-flow pattern.
 */
internal sealed class SyntheticDestinationOutcome : RuntimeException() {

    internal class Open(
        val target: NavigationKey.Instance<NavigationKey>,
    ) : SyntheticDestinationOutcome()

    /** A close with [silent] `false` calls the result-channel callback. A silent close does not. */
    internal class Close(
        val silent: Boolean,
    ) : SyntheticDestinationOutcome()

    internal class Complete(
        val result: Any?,
    ) : SyntheticDestinationOutcome()

    internal class CompleteFrom(
        val target: NavigationKey.Instance<NavigationKey>,
    ) : SyntheticDestinationOutcome()

    internal class SideEffect(
        val block: SyntheticSideEffectScope<NavigationKey>.() -> Unit,
    ) : SyntheticDestinationOutcome()
}
