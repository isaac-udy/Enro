package dev.enro.ui.destinations

import dev.enro.NavigationKey
import dev.enro.asInstance
import kotlin.jvm.JvmName

/**
 * Ends the synthetic's block and registers a `Completed` result with [result] for the caller
 * that opened the synthetic.
 */
public fun <R : Any> SyntheticDestinationScope<out NavigationKey.WithResult<R>>.complete(
    result: R,
): Nothing = setOutcome(SyntheticDestinationOutcome.Complete(result = result))

/**
 * Ends the synthetic's block and opens [key]. The result of [key] is delivered to the caller
 * that opened the synthetic.
 */
public fun <R : Any> SyntheticDestinationScope<out NavigationKey.WithResult<R>>.completeFrom(
    key: NavigationKey.WithResult<R>,
): Nothing = setOutcome(SyntheticDestinationOutcome.CompleteFrom(key.asInstance()))

public fun <R : Any> SyntheticDestinationScope<out NavigationKey.WithResult<R>>.completeFrom(
    key: NavigationKey.WithMetadata<NavigationKey.WithResult<R>>,
): Nothing = setOutcome(SyntheticDestinationOutcome.CompleteFrom(key.asInstance()))

@JvmName("completeWithoutResult")
@Deprecated(
    message = "A synthetic for a NavigationKey.WithResult cannot complete without a result. Use complete(result) instead.",
    level = DeprecationLevel.ERROR,
)
public fun <R : Any> SyntheticDestinationScope<out NavigationKey.WithResult<R>>.complete(): Nothing {
    error("${instance.key} is a NavigationKey.WithResult and cannot complete without a result")
}

@JvmName("completeFromNonResultDeprecated")
@Deprecated(
    message = "A synthetic for a NavigationKey.WithResult cannot completeFrom a NavigationKey that does not also implement NavigationKey.WithResult of the same result type.",
    level = DeprecationLevel.ERROR,
)
public fun <R : Any> SyntheticDestinationScope<out NavigationKey.WithResult<R>>.completeFrom(
    key: NavigationKey,
): Nothing {
    error("${instance.key} is a NavigationKey.WithResult and cannot completeFrom a key that is not also a NavigationKey.WithResult of the same result type")
}
