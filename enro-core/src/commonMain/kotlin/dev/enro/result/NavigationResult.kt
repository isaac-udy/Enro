package dev.enro.result

import dev.enro.NavigationKey
import kotlin.jvm.JvmName

public sealed class NavigationResult<K : NavigationKey> {
    public class Closed : NavigationResult<NavigationKey>()

    public class Delegated(
        public val id: String,
    ) : NavigationResult<NavigationKey>()

    public class Completed<K : NavigationKey>(
        @PublishedApi
        internal val data: Any?
    ) : NavigationResult<K>() {
        public companion object {
            public val <R : Any> Completed<out NavigationKey.WithResult<R>>.result: R get() {
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

@PublishedApi
internal fun NavigationKey.Instance<NavigationKey>.clearResult() {
    metadata.remove(NavigationResult.MetadataKey)
}

@PublishedApi
internal fun <K : NavigationKey> NavigationKey.Instance<K>.getResult(): NavigationResult<K> {
    @Suppress("UNCHECKED_CAST")
    return metadata.get(NavigationResult.MetadataKey) as NavigationResult<K>
}
@PublishedApi
internal fun NavigationKey.Instance<NavigationKey>.setResultClosed() {
    metadata.set(NavigationResult.MetadataKey, NavigationResult.Closed())
}

@PublishedApi
internal fun NavigationKey.Instance<NavigationKey>.setResultCompleted() {
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
internal fun <R: Any> NavigationKey.Instance<NavigationKey.WithResult<R>>.setResultCompleted() {
    error("$key is a NavigationKey.WithResult and cannot be completed without a result")
}

internal fun <R: Any> NavigationKey.Instance<NavigationKey.WithResult<R>>.setResultCompleted(result: R) {
    metadata.set(NavigationResult.MetadataKey, NavigationResult.Completed<NavigationKey.WithResult<R>>(result))
}

@JvmName("setDelegatedResultGeneric")
@PublishedApi
internal fun NavigationKey.Instance<NavigationKey>.setDelegatedResult(
    instance: NavigationKey.Instance<NavigationKey>,
) {
    metadata.set(NavigationResult.MetadataKey, NavigationResult.Delegated(instance.id))
}

@JvmName("setDelegatedResultDeprecated")
@Deprecated(
    message = "A NavigationKey.WithResult cannot delegate a result to a key that does not match its result type",
    level = DeprecationLevel.ERROR,
)
internal fun <R: Any> NavigationKey.Instance<NavigationKey.WithResult<R>>.setDelegatedResult(
    instance: NavigationKey.Instance<NavigationKey>,
) {
    error("$key is a NavigationKey.WithResult and cannot delegate a result to a key that does not match its result type")
}

@PublishedApi
internal fun <R: Any> NavigationKey.Instance<NavigationKey.WithResult<R>>.setDelegatedResult(
    instance: NavigationKey.Instance<NavigationKey.WithResult<R>>,
) {
    metadata.set(NavigationResult.MetadataKey, NavigationResult.Delegated(instance.id))
}