package dev.enro.result

import dev.enro.NavigationKey
import kotlin.jvm.JvmName

public sealed class NavigationResult<K : NavigationKey> {
    @PublishedApi
    internal class Closed : NavigationResult<NavigationKey>()

    @PublishedApi
    internal class Delegated(
        val id: String,
    ) : NavigationResult<NavigationKey>()

    @PublishedApi
    internal class Completed<K : NavigationKey>(
        @PublishedApi
        internal val data: Any?
    ) : NavigationResult<K>() {
        companion object {
            val <R : Any> Completed<out NavigationKey.WithResult<R>>.result: R get() {
                require(data != null) {
                    "Incorrect type, but got null"
                }
                @Suppress("UNCHECKED_CAST")
                return data as R
            }
        }
    }

    @PublishedApi
    internal object MetadataKey : NavigationKey.TransientMetadataKey<NavigationResult<*>>(
        default = Closed(),
    )
}

internal fun NavigationKey.Instance<out NavigationKey>.clearResult() {
    metadata.remove(NavigationResult.MetadataKey)
}

@PublishedApi
internal fun <K : NavigationKey> NavigationKey.Instance<K>.getResult(): NavigationResult<K> {
    @Suppress("UNCHECKED_CAST")
    return metadata.get(NavigationResult.MetadataKey) as NavigationResult<K>
}

internal fun NavigationKey.Instance<out NavigationKey>.setResultClosed() {
    metadata.set(NavigationResult.MetadataKey, NavigationResult.Closed())
}

internal fun NavigationKey.Instance<out NavigationKey>.setResultCompleted() {
    require(key !is NavigationKey.WithResult<*>) {
        "${key::class} is not a NavigationKey.WithResult and cannot be completed"
    }
    metadata.set(NavigationResult.MetadataKey, NavigationResult.Completed<NavigationKey>(Unit))
}

@JvmName("setResultCompletedWithoutResult")
@Deprecated(
    message = "A NavigationKey.WithResult should not be completed without a result, doing so will result in an error",
    level = DeprecationLevel.ERROR,
)
internal fun <R: Any> NavigationKey.Instance<out NavigationKey.WithResult<R>>.setResultCompleted() {
    error("$key is a NavigationKey.WithResult and cannot be completed without a result")
}

internal fun <R: Any> NavigationKey.Instance<out NavigationKey.WithResult<R>>.setResultCompleted(result: R) {
    metadata.set(NavigationResult.MetadataKey, NavigationResult.Completed<NavigationKey.WithResult<R>>(result))
}

@JvmName("setDelegatedResultGeneric")
internal fun NavigationKey.Instance<out NavigationKey>.setDelegatedResult(
    instance: NavigationKey.Instance<out NavigationKey>,
) {
    metadata.set(NavigationResult.MetadataKey, NavigationResult.Delegated(instance.id))
}

@JvmName("setDelegatedResultDeprecated")
@Deprecated(
    message = "A NavigationKey.WithResult cannot delegate a result to a key that does not match its result type",
    level = DeprecationLevel.ERROR,
)
internal fun <R: Any> NavigationKey.Instance<out NavigationKey.WithResult<R>>.setDelegatedResult(
    instance: NavigationKey.Instance<out NavigationKey>,
) {
    error("$key is a NavigationKey.WithResult and cannot delegate a result to a key that does not match its result type")
}

internal fun <R: Any> NavigationKey.Instance<out NavigationKey.WithResult<R>>.setDelegatedResult(
    instance: NavigationKey.Instance<out NavigationKey.WithResult<R>>,
) {
    metadata.set(NavigationResult.MetadataKey, NavigationResult.Delegated(instance.id))
}