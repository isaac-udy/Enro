package dev.enro.ui.destinations

import dev.enro.NavigationKey
import dev.enro.asInstance
import kotlin.jvm.JvmName

/**
 * End the synthetic's outcome decision by registering a `Completed` result
 * with the given [result] against whoever opened the synthetic. Only
 * available when the synthetic's key is a [NavigationKey.WithResult].
 */
public fun <R : Any> SyntheticDestinationScope<out NavigationKey.WithResult<R>>.complete(
    result: R,
): Nothing = throw SyntheticDestinationOutcome.Complete(result = result)

/**
 * End the synthetic's outcome decision by opening [key] and routing its
 * eventual completion back to whoever opened the synthetic. The result
 * type of [key] must match the synthetic's contract.
 */
public fun <R : Any> SyntheticDestinationScope<out NavigationKey.WithResult<R>>.completeFrom(
    key: NavigationKey.WithResult<R>,
): Nothing = throw SyntheticDestinationOutcome.CompleteFrom(key.asInstance())

public fun <R : Any> SyntheticDestinationScope<out NavigationKey.WithResult<R>>.completeFrom(
    key: NavigationKey.WithMetadata<NavigationKey.WithResult<R>>,
): Nothing = throw SyntheticDestinationOutcome.CompleteFrom(key.asInstance())

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
