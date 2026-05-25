package dev.enro

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import kotlin.jvm.JvmName

/**
 * The active destination's view of the navigation framework.
 *
 * A [NavigationHandle] represents a single live entry on the backstack —
 * one instance, one lifecycle, one [SavedStateHandle]. From inside a
 * destination you use it to talk back to the navigation system: open
 * another destination, close yourself, complete with a result, etc.
 *
 * Get one via:
 * - `navigationHandle()` inside a `@Composable` destination
 * - `by navigationHandle()` as a delegated property in a `ViewModel`
 * - `viewModelStoreOwner.getNavigationHandle<MyKey>()` from a Fragment / Activity
 *
 * The handle is always typed to the [NavigationKey] subtype that started
 * the destination, so completion APIs that depend on the key's result type
 * (see [NavigationKey.WithResult]) stay statically checked.
 *
 * Implements [LifecycleOwner]: the handle's lifecycle tracks the
 * destination's lifecycle (created → started → resumed → … → destroyed),
 * so anything scoped to it (coroutines, observers) cleans up automatically
 * when the destination leaves the backstack.
 *
 * @param T the [NavigationKey] subtype this handle was created for.
 */
public abstract class NavigationHandle<out T : NavigationKey> internal constructor() : LifecycleOwner {
    /**
     * State that survives configuration changes and process death for this
     * destination. Wraps the platform's `SavedStateHandle` and is scoped to
     * this entry — different backstack entries for the same key each get
     * their own.
     */
    public abstract val savedStateHandle: SavedStateHandle

    /**
     * The backstack entry this handle was attached to: the [NavigationKey]
     * plus a stable id and any metadata layered onto it. Prefer [instance]
     * over [key] when you need the id (e.g. to compare against other
     * backstack entries) or metadata; use [key] when you only need the
     * key's data.
     */
    public abstract val instance: NavigationKey.Instance<T>

    /**
     * Shorthand for `instance.key`. Same value object that the caller
     * passed to `open(…)` / put in the initial backstack — its properties
     * are the key's serialised fields.
     */
    public val key: T get() = instance.key

    @Deprecated("Use instance")
    public val instruction: NavigationKey.Instance<T> get() = instance

    /**
     * Low-level entry point that dispatches a [NavigationOperation] through
     * the controller. The strongly-typed helpers ([open], [close],
     * [complete], [completeFrom], [closeAndReplaceWith],
     * [closeAndCompleteFrom]) cover every common case; call [execute]
     * directly only when you need to compose an operation those helpers
     * don't already build — e.g. a custom [NavigationOperation.SetBackstack]
     * or a hand-rolled [NavigationOperation.AggregateOperation].
     */
    public abstract fun execute(
        operation: NavigationOperation,
    )
}

/**
 * Asks the destination to close itself, routing through any
 * `onCloseRequested` callback registered via [NavigationHandleConfiguration].
 *
 * If a callback is registered it runs in place of the default close — that's
 * how a destination opts in to "prompt before dismissing" behaviour by
 * showing a confirmation dialog and only invoking [close] when the user
 * confirms. With no callback registered this is equivalent to [close].
 *
 * This is the function predictive-back and system-back gestures invoke, so
 * if you want gesture-triggered close to route through your confirmation
 * UI, register the callback (and DO NOT call [close] directly elsewhere).
 */
public fun NavigationHandle<*>.requestClose() {
    NavigationHandleConfiguration.onCloseRequested(this)
}

/**
 * Closes this destination unconditionally — removes it from the backstack
 * and tears down its lifecycle.
 *
 * Bypasses any registered `onCloseRequested` callback; use [requestClose]
 * if you want the registered callback to have a chance to intervene
 * (typical for system-back / "X" button affordances).
 */
public fun NavigationHandle<*>.close() {
    execute(NavigationOperation.Close(instance))
}

/**
 * Completes a non-result destination — closes this entry and notifies
 * the opener that the work finished successfully.
 *
 * Use this for navigation keys with no result payload. For
 * [NavigationKey.WithResult] keys, call the result-carrying overload
 * `complete(result)` instead — completing a result key without a result
 * is a programming error and produces a deprecation-level error.
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
 * Completes this result-bearing destination with [result], closes it, and
 * delivers the value back to whoever opened it.
 *
 * The opener receives the result through the `registerForNavigationResult`
 * channel they set up before opening this key.
 */
public fun <R : Any> NavigationHandle<NavigationKey.WithResult<R>>.complete(result: R) {
    execute(NavigationOperation.Complete(instance, result))
}

/**
 * Forwards completion of this destination to another navigation [key].
 *
 * Used by *intermediary* destinations that delegate to a downstream
 * destination: this entry closes, [key] is opened in its place, and when
 * [key] eventually completes its result is delivered to whoever originally
 * opened *this* entry. Lets you wedge a chooser / disambiguator in front
 * of a result-producing destination without the opener knowing.
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
 * Type-safe [completeFrom] for result destinations: forwards completion
 * to [key], which must produce a result of the same type [R] this handle
 * is expecting. The compiler rejects mismatched result types.
 */
public fun <R : Any> NavigationHandle<NavigationKey.WithResult<R>>.completeFrom(key: NavigationKey.WithResult<R>) {
    execute(NavigationOperation.CompleteFrom(instance, key.asInstance()))
}

/**
 * [completeFrom] variant that accepts a [NavigationKey.WithMetadata]
 * wrapper around the forwarded key — use this when the destination you
 * want to forward to was built with extra metadata (e.g. result-channel
 * routing data attached via `withMetadata { … }`).
 */
public fun <R : Any> NavigationHandle<NavigationKey.WithResult<R>>.completeFrom(key: NavigationKey.WithMetadata<NavigationKey.WithResult<R>>) {
    execute(NavigationOperation.CompleteFrom(instance, key.asInstance()))
}

/**
 * Pushes a new destination for [key] on top of the current backstack.
 *
 * Doesn't close the calling destination — both stay on the stack. Use
 * [closeAndReplaceWith] when you want the new destination to take this
 * one's place.
 */
public fun NavigationHandle<*>.open(key: NavigationKey) {
    execute(NavigationOperation.Open(key.asInstance()))
}

/**
 * [open] variant that accepts a [NavigationKey.WithMetadata] wrapper —
 * use this when the destination needs metadata attached (e.g. a result
 * channel id from `registerForNavigationResult`, or transition overrides).
 */
public fun NavigationHandle<*>.open(key: NavigationKey.WithMetadata<*>) {
    execute(NavigationOperation.Open(key.asInstance()))
}

/**
 * Closes this destination and opens [key] in one atomic operation — the
 * backstack transitions directly from `[…, this]` to `[…, key]` with no
 * intermediate state.
 *
 * Equivalent to a `close()` followed by an `open(key)` from the previous
 * destination's perspective, but bundled so that interceptors,
 * animations, and saved-state cleanup all see it as a single operation.
 * Prefer this over manual `close(); open(key)` when you want a clean
 * "replace this screen with that one" effect.
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
 * [closeAndReplaceWith] variant accepting a [NavigationKey.WithMetadata]
 * wrapper for the replacement key.
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
 * Closes this destination AND forwards completion to [key] atomically.
 *
 * Compared to [completeFrom], this one also removes the current
 * destination from the backstack immediately — useful when you don't want
 * the intermediary to stay visible underneath while the forwarded
 * destination is open. Common shape: a "choose flavour" screen
 * closes-and-completes-from the variant-specific result destination.
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
 * Type-safe [closeAndCompleteFrom] for result destinations: [key] must
 * produce a result of the same type [R] this handle is expecting.
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
 * [closeAndCompleteFrom] variant that accepts a [NavigationKey.WithMetadata]
 * wrapper around the forwarded key.
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
