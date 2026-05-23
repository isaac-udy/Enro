package dev.enro.core.result

import dev.enro.NavigationKey
import dev.enro.core.NavigationDirection
import dev.enro.ui.destinations.SyntheticDestinationScope
import dev.enro.ui.destinations.complete
import dev.enro.ui.destinations.completeFrom

@Deprecated(
    message = "Use SyntheticDestinationScope.complete(result) instead. The direction concept from Enro 2 is no longer relevant; the new method registers the same Completed result against the synthetic's result channel.",
    replaceWith = ReplaceWith(
        expression = "complete(result)",
        imports = ["dev.enro.ui.destinations.complete"],
    ),
)
public fun <T : Any> SyntheticDestinationScope<out NavigationKey.WithResult<T>>.sendResult(
    result: T,
): Nothing = complete(result)

@Deprecated(
    message = "Use SyntheticDestinationScope.completeFrom(navigationKey) instead. The direction parameter is no longer used in Enro 3 — push/present semantics are now expressed elsewhere.",
    replaceWith = ReplaceWith(
        expression = "completeFrom(navigationKey)",
        imports = ["dev.enro.ui.destinations.completeFrom"],
    ),
)
@Suppress("UNUSED_PARAMETER")
public fun <T : Any> SyntheticDestinationScope<out NavigationKey.WithResult<T>>.forwardResult(
    navigationKey: NavigationKey.WithResult<T>,
    direction: NavigationDirection = when (navigationKey) {
        is dev.enro.core.NavigationKey.SupportsPresent -> NavigationDirection.Present
        else -> NavigationDirection.Push
    },
): Nothing = completeFrom(navigationKey)
