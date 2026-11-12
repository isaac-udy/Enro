package dev.enro

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import kotlin.jvm.JvmName

/**
 * The API a destination uses to open, close and complete destinations.
 *
 * A [NavigationHandle] belongs to one entry on the backstack, with one
 * [NavigationKey.Instance], one lifecycle and one [SavedStateHandle].
 *
 * Get a [NavigationHandle] with:
 * - `navigationHandle()` inside a `@Composable` destination
 * - `by navigationHandle()` as a delegated property in a `ViewModel`
 * - `viewModelStoreOwner.getNavigationHandle<MyKey>()` from a Fragment or Activity
 *
 * The lifecycle of the [NavigationHandle] follows the lifecycle of the
 * destination and is destroyed when the destination is removed from the
 * backstack.
 *
 * @param T the [NavigationKey] type of the destination.
 */
public abstract class NavigationHandle<out T : NavigationKey> internal constructor() : LifecycleOwner {
    /**
     * Saved state scoped to this backstack entry. Two backstack entries for
     * the same key have separate [SavedStateHandle] instances.
     */
    public abstract val savedStateHandle: SavedStateHandle

    /**
     * The backstack entry of this handle: the [NavigationKey], the id of
     * the entry and the metadata of the entry.
     */
    public abstract val instance: NavigationKey.Instance<T>

    public val key: T get() = instance.key

    @Deprecated("Use instance")
    public val instruction: NavigationKey.Instance<T> get() = instance

    /**
     * Executes [operation]. [open], [close], [complete], [completeFrom],
     * [closeAndReplaceWith] and [closeAndCompleteFrom] build an operation
     * and call [execute]. Call [execute] directly for an operation that
     * those functions do not build, such as a
     * [NavigationOperation.SetBackstack] or a custom
     * [NavigationOperation.AggregateOperation].
     */
    public abstract fun execute(
        operation: NavigationOperation,
    )
}

/**
 * Runs the `onCloseRequested` callback registered through
 * [NavigationHandleConfiguration] in place of closing the destination. The
 * callback decides whether to call [close]. With no callback registered,
 * [requestClose] is equivalent to [close]. With more than one callback
 * registered, [requestClose] throws [IllegalStateException].
 *
 * Predictive-back and system-back gestures call [requestClose].
 */
public fun NavigationHandle<*>.requestClose() {
    NavigationHandleConfiguration.onCloseRequested(this)
}

/**
 * Removes this destination from the backstack without running the
 * `onCloseRequested` callback. [requestClose] runs the callback.
 */
public fun NavigationHandle<*>.close() {
    execute(NavigationOperation.Close(instance))
}

/**
 * Closes this destination and delivers a completed result to the caller
 * that opened this destination.
 *
 * A destination for a [NavigationKey.WithResult] must call
 * `complete(result)`: calling [complete] without a result on a
 * [NavigationKey.WithResult] handle is a compile error.
 */
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

/**
 * Closes this destination and delivers [result] to the
 * `registerForNavigationResult` channel that opened this destination.
 */
public fun <R : Any> NavigationHandle<NavigationKey.WithResult<R>>.complete(result: R) {
    execute(NavigationOperation.Complete(instance, result))
}

/**
 * Forwards completion of this destination to another navigation [key].
 *
 * Opens [key] and leaves this destination on the backstack. When the [key]
 * destination completes, the result is delivered to the caller that opened
 * this destination. [closeAndCompleteFrom] also closes this destination.
 *
 * For result-typed destinations, prefer the [NavigationKey.WithResult]
 * overload, which enforces matching result types at compile time.
 */
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

/**
 * [completeFrom] for a [NavigationKey.WithResult] destination. [key] must
 * have the same result type [R] as this destination.
 */
public fun <R : Any> NavigationHandle<NavigationKey.WithResult<R>>.completeFrom(key: NavigationKey.WithResult<R>) {
    execute(NavigationOperation.CompleteFrom(instance, key.asInstance()))
}

/**
 * [completeFrom] for a [key] that has metadata attached.
 */
public fun <R : Any> NavigationHandle<NavigationKey.WithResult<R>>.completeFrom(key: NavigationKey.WithMetadata<NavigationKey.WithResult<R>>) {
    execute(NavigationOperation.CompleteFrom(instance, key.asInstance()))
}

/**
 * Opens a destination for [key] and leaves this destination on the
 * backstack. [closeAndReplaceWith] also closes this destination.
 */
public fun NavigationHandle<*>.open(key: NavigationKey) {
    execute(NavigationOperation.Open(key.asInstance()))
}

/**
 * [open] for a [key] that has metadata attached.
 */
public fun NavigationHandle<*>.open(key: NavigationKey.WithMetadata<*>) {
    execute(NavigationOperation.Open(key.asInstance()))
}

/**
 * Closes this destination and opens [key] as one
 * [NavigationOperation.AggregateOperation]. The backstack changes from
 * `[…, this]` to `[…, key]` in one update.
 */
public fun NavigationHandle<*>.closeAndReplaceWith(key: NavigationKey) {
    execute(
        NavigationOperation.AggregateOperation(
            NavigationOperation.Close(instance),
            NavigationOperation.Open(key.asInstance()),
        )
    )
}

/**
 * [closeAndReplaceWith] for a [key] that has metadata attached.
 */
public fun NavigationHandle<*>.closeAndReplaceWith(key: NavigationKey.WithMetadata<*>) {
    execute(
        NavigationOperation.AggregateOperation(
            NavigationOperation.Close(instance),
            NavigationOperation.Open(key.asInstance()),
        )
    )
}

/**
 * Closes this destination and opens [key] as one
 * [NavigationOperation.AggregateOperation]. When the [key] destination
 * completes, the result is delivered to the caller that opened this
 * destination. [completeFrom] leaves this destination on the backstack.
 */
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

/**
 * [closeAndCompleteFrom] for a [NavigationKey.WithResult] destination.
 * [key] must have the same result type [R] as this destination.
 */
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

/**
 * [closeAndCompleteFrom] for a [key] that has metadata attached.
 */
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
