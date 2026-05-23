package dev.enro.ui.destinations

import dev.enro.NavigationKey

/**
 * Sentinel exception thrown by the outcome methods on [SyntheticDestinationScope]
 * (e.g. `open`, `close`, `complete`, `completeFrom`) to signal the synthetic's
 * decision back up to the synthetic dispatcher. The dispatcher catches the
 * subclass and converts it to the corresponding [dev.enro.NavigationOperation].
 *
 * Throwing for control flow mirrors the pattern used by [InterceptorBuilderResult][dev.enro.interceptor.builder.InterceptorBuilderResult]
 * — it lets the scope methods return [Nothing] so the synthetic block can
 * short-circuit naturally from inside conditionals.
 */
@PublishedApi
internal sealed class SyntheticDestinationOutcome : RuntimeException() {

    internal class Open(
        val target: NavigationKey.Instance<NavigationKey>,
    ) : SyntheticDestinationOutcome()

    internal class Close : SyntheticDestinationOutcome()

    internal class Complete(
        val result: Any?,
    ) : SyntheticDestinationOutcome()

    internal class CompleteFrom(
        val target: NavigationKey.Instance<NavigationKey>,
    ) : SyntheticDestinationOutcome()
}
