package dev.enro

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import dev.enro.annotations.AdvancedEnroApi
import kotlin.jvm.JvmName

public abstract class NavigationHandle<out T : NavigationKey> internal constructor() : LifecycleOwner {
    public abstract val savedStateHandle: SavedStateHandle

    public abstract val instance: NavigationKey.Instance<T>
    public val key: T get() = instance.key

    @AdvancedEnroApi
    public abstract fun execute(
        operation: NavigationOperation,
    )
}

public fun NavigationHandle<*>.close() {
    execute(NavigationOperation.Close(instance))
}

public fun NavigationHandle<*>.closeWithoutCallback() {
    instance.metadata.set(NavigationHandleConfiguration.OnCloseCallbacksEnabled, false)
    try {
        execute(NavigationOperation.Close(instance))
    } finally {
        instance.metadata.set(NavigationHandleConfiguration.OnCloseCallbacksEnabled, true)
    }
}

public fun NavigationHandle<*>.complete() {
    execute(NavigationOperation.Complete(instance))
}

@JvmName("completeWithoutResult")
@Deprecated(
    message = "A NavigationKey.WithResult should not be completed without a result, doing so will result in an error",
    level = DeprecationLevel.ERROR,
)
public fun <R : Any> NavigationHandle<NavigationKey.WithResult<R>>.complete() {
    error("${instance.key} is a NavigationKey.WithResult and cannot be completed without a result")
}

public fun <R : Any> NavigationHandle<NavigationKey.WithResult<R>>.complete(result: R) {
    execute(NavigationOperation.Complete(instance, result))
}

public fun NavigationHandle<NavigationKey>.completeFrom(key: NavigationKey) {
    execute(NavigationOperation.CompleteFrom(instance, key.asInstance()))
}

@JvmName("completeFromGeneric")
@Deprecated(
    message = "A NavigationKey.WithResult cannot complete from a NavigationKey that does not have a result",
    level = DeprecationLevel.ERROR,
)
public fun <R : Any> NavigationHandle<NavigationKey.WithResult<R>>.completeFrom(key: NavigationKey) {
    error("${instance.key} is a NavigationKey.WithResult and cannot complete from a NavigationKey that does not have a result")
}

public fun <R : Any> NavigationHandle<NavigationKey.WithResult<R>>.completeFrom(key: NavigationKey.WithResult<R>) {
    execute(NavigationOperation.CompleteFrom(instance, key.asInstance()))
}

public fun <R : Any> NavigationHandle<NavigationKey.WithResult<R>>.completeFrom(key: NavigationKey.WithMetadata<NavigationKey.WithResult<R>>) {
    execute(NavigationOperation.CompleteFrom(instance, key.asInstance()))
}

public fun NavigationHandle<*>.open(key: NavigationKey) {
    execute(NavigationOperation.Open(key.asInstance()))
}

public fun NavigationHandle<*>.open(key: NavigationKey.WithMetadata<*>) {
    execute(NavigationOperation.Open(key.asInstance()))
}

public fun NavigationHandle<*>.closeAndReplaceWith(key: NavigationKey) {
    execute(
        NavigationOperation.AggregateOperation(
            NavigationOperation.Close(instance),
            NavigationOperation.Open(key.asInstance()),
        )
    )
}

public fun NavigationHandle<*>.closeAndReplaceWith(key: NavigationKey.WithMetadata<*>) {
    execute(
        NavigationOperation.AggregateOperation(
            NavigationOperation.Close(instance),
            NavigationOperation.Open(key.asInstance()),
        )
    )
}

public fun NavigationHandle<NavigationKey>.closeAndCompleteFrom(key: NavigationKey) {
    execute(
        NavigationOperation.AggregateOperation(
            NavigationOperation.Close(instance),
            NavigationOperation.CompleteFrom(instance, key.asInstance())
        )
    )
}

@JvmName("closeAndCompleteFromGeneric")
@Deprecated(
    message = "A NavigationKey.WithResult cannot complete from a NavigationKey that does not have a result",
    level = DeprecationLevel.ERROR,
)
public fun <R : Any> NavigationHandle<NavigationKey.WithResult<R>>.closeAndCompleteFrom(key: NavigationKey) {
    error("${instance.key} is a NavigationKey.WithResult and cannot complete from a NavigationKey that does not have a result")
}

public fun <R : Any> NavigationHandle<NavigationKey.WithResult<R>>.closeAndCompleteFrom(
    key: NavigationKey.WithResult<R>,
) {
    execute(
        NavigationOperation.AggregateOperation(
            NavigationOperation.Close(instance),
            NavigationOperation.CompleteFrom(instance, key.asInstance())
        )
    )
}

public fun <R : Any> NavigationHandle<NavigationKey.WithResult<R>>.closeAndCompleteFrom(
    key: NavigationKey.WithMetadata<NavigationKey.WithResult<R>>,
) {
    execute(
        NavigationOperation.AggregateOperation(
            NavigationOperation.Close(instance),
            NavigationOperation.CompleteFrom(instance, key.asInstance())
        )
    )
}