package dev.enro3

import androidx.lifecycle.LifecycleOwner
import dev.enro.annotations.AdvancedEnroApi
import kotlin.jvm.JvmName

public abstract class NavigationHandle<T : NavigationKey> internal constructor() : LifecycleOwner {
    public abstract val instance: NavigationKey.Instance<T>

    public val id: String get() = instance.id
    public val key: T get() = instance.key

    @AdvancedEnroApi
    public abstract fun execute(operation: NavigationOperation)
}

public fun NavigationHandle<*>.close() {
    execute(NavigationOperation.Companion.close(instance))
}

public fun NavigationHandle<*>.complete() {
    execute(NavigationOperation.Companion.complete(instance))
}

@JvmName("completeWithoutResult")
@Deprecated(
    message = "A NavigationKey.WithResult should not be completed without a result, doing so will result in an error",
    level = DeprecationLevel.ERROR,
)
public fun <R: Any> NavigationHandle<out NavigationKey.WithResult<R>>.complete() {
    error("${instance.key} is a NavigationKey.WithResult and cannot be completed without a result")
}

public fun <R : Any> NavigationHandle<out NavigationKey.WithResult<R>>.complete(result: R) {
    execute(NavigationOperation.Companion.complete(instance, result))
}

public fun NavigationHandle<out NavigationKey>.completeFrom(key: NavigationKey) {
    execute(NavigationOperation.Companion.completeFrom(instance, key.asInstance()))
}

@JvmName("completeFromGeneric")
@Deprecated(
    message = "A NavigationKey.WithResult cannot complete from a NavigationKey that does not have a result",
    level = DeprecationLevel.ERROR,
)
public fun <R : Any> NavigationHandle<out NavigationKey.WithResult<R>>.completeFrom(key: NavigationKey) {
    error("${instance.key} is a NavigationKey.WithResult and cannot complete from a NavigationKey that does not have a result")
}

public fun <R : Any> NavigationHandle<out NavigationKey.WithResult<R>>.completeFrom(key: NavigationKey.WithResult<R>) {
    execute(NavigationOperation.Companion.completeFrom(instance, key.asInstance()))
}

public fun <R : Any> NavigationHandle<out NavigationKey.WithResult<R>>.completeFrom(key: NavigationKey.WithMetadata<out NavigationKey.WithResult<R>>) {
    execute(NavigationOperation.Companion.completeFrom(instance, key.asInstance()))
}

public fun NavigationHandle<*>.open(key: NavigationKey) {
    execute(NavigationOperation.Companion.open(key.asInstance()))
}

public fun NavigationHandle<*>.open(key: NavigationKey.WithMetadata<*>) {
    execute(NavigationOperation.Companion.open(key.asInstance()))
}

