package dev.enro

import dev.enro.result.NavigationResultChannel
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic

public fun <K : NavigationKey> NavigationKey.Instance<K>.asOpenOperation(): NavigationOperation.Open<K> {
    return NavigationOperation.Open(this)
}

public fun <K : NavigationKey> NavigationKey.Instance<K>.asCloseOperation(): NavigationOperation.Close<K> {
    return NavigationOperation.Close(this)
}

@JvmName("complete")
public fun NavigationKey.Instance<NavigationKey>.asCompleteOperation(): NavigationOperation.Complete<NavigationKey> {
    return NavigationOperation.Complete(this)
}

@JvmName("completeWithoutResult")
@Deprecated(
    message = "A NavigationKey.WithResult should not be completed without a result, doing so will result in an error",
    level = DeprecationLevel.ERROR,
)
public fun <R : Any> NavigationKey.Instance<NavigationKey.WithResult<R>>.asCompleteOperation(
): NavigationOperation.Complete<NavigationKey> {
    error("${this.key} is a NavigationKey.WithResult and cannot be completed without a result")
}

@JvmName("complete")
public fun <R : Any> NavigationKey.Instance<NavigationKey.WithResult<R>>.asCompleteOperation(
    result: R,
): NavigationOperation.Complete<NavigationKey.WithResult<R>> {
    return NavigationOperation.Complete(this, result)
}

@JvmName("completeFromWithoutResult")
public fun NavigationKey.Instance<NavigationKey>.asCompleteFromOperation(
    completeFrom: NavigationKey.Instance<NavigationKey>,
): NavigationOperation.Open<NavigationKey> {
    completeFrom.metadata.set(
        NavigationResultChannel.ResultIdKey,
        this.metadata.get(NavigationResultChannel.ResultIdKey)
    )
    return NavigationOperation.Open(completeFrom)
}

@Deprecated(
    message = "A NavigationKey.WithResult cannot completeFrom a NavigationKey that does not also implement NavigationKey.WithResult",
    level = DeprecationLevel.ERROR,
)
@JvmName("completeFromWithoutResultDeprecated")
public fun <R : Any> NavigationKey.Instance<NavigationKey>.asCompleteFromOperation(
    completeFrom: NavigationKey.Instance<NavigationKey.WithResult<R>>,
): NavigationOperation {
    error("Cannot completeFrom a NavigationKey.WithResult from a NavigationKey that does not also implement NavigationKey.WithResult")
}

@JvmName("completeFrom")
public fun <R : Any> NavigationKey.Instance<NavigationKey.WithResult<R>>.asCompleteFromOperation(
    completeFrom: NavigationKey.Instance<NavigationKey.WithResult<R>>,
): NavigationOperation.Open<NavigationKey.WithResult<R>> {
    completeFrom.metadata.set(
        NavigationResultChannel.ResultIdKey,
        this.metadata.get(NavigationResultChannel.ResultIdKey)
    )
    return NavigationOperation.Open<NavigationKey.WithResult<R>>(
        instance = completeFrom,
    )
}