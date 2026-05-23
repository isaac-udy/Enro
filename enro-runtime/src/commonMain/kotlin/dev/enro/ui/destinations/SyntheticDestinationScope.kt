package dev.enro.ui.destinations

import dev.enro.NavigationContext
import dev.enro.NavigationKey
import dev.enro.asInstance
import dev.enro.context.AnyNavigationContext
import dev.enro.context.ContainerContext
import dev.enro.context.DestinationContext
import dev.enro.context.RootContext

/**
 * The receiver scope of a `syntheticDestination { ... }` block. A synthetic
 * destination is a [NavigationKey] that, when opened, runs the block instead
 * of rendering a UI; the synthetic instance never reaches a backstack.
 *
 * The block can return without calling any outcome method — in that case the
 * synthetic acts purely as a side-effect bridge (e.g. launching an `Intent`
 * to Chrome Custom Tabs) and the synthetic is considered to have closed
 * silently. No result-channel callback fires for the original caller.
 *
 * Alternatively, the block can short-circuit by calling one of the outcome
 * methods:
 *
 * - [open] — open another [NavigationKey] in place of the synthetic.
 * - [close] — register a `Closed` result for whoever called the synthetic.
 * - [closeSilently] — close without firing the result-channel callback.
 * - [complete] — register a `Completed` result. For result-bearing
 *   synthetics see the `complete(result: T)` extension.
 * - [completeFrom] — open another key and have *its* completion fulfil the
 *   synthetic's contract. Useful for "decider" synthetics that pick one of
 *   several destinations at runtime.
 *
 * Each outcome method throws a sentinel and returns [Nothing], so calls
 * inside conditionals flow naturally without needing explicit `return`.
 *
 * The scope tracks the outcome it settles on. Once an outcome is set —
 * whether by an explicit method call inside the block, or by the dispatcher
 * defaulting to a silent close after the block returns — any subsequent
 * call (typically from an async coroutine that outlived the block) throws
 * a clear error instead of silently double-handling.
 */
public class SyntheticDestinationScope<K : NavigationKey> @PublishedApi internal constructor(
    // context is the NavigationContext that is executing this SyntheticDestination,
    // which could be a RootContext, ContainerContext or DestinationContext depending on how
    // the synthetic destination was opened
    public val context: NavigationContext,
    public val instance: NavigationKey.Instance<K>,
) {
    public val key: K = instance.key

    // destinationContext will be the active destination closest to the context,
    // meaning that if context is a DestinationContext, destinationContext will be that instance,
    // if context is a ContainerContext, destinationContext will be that container's active context,
    // and if the context is a RootContext, destinationContext will be the active child of the RootContext's
    // active ContainerContext
    public val destinationContext: DestinationContext<NavigationKey>?
        get() = when (context) {
            is DestinationContext<*> -> context
            is ContainerContext -> context.activeChild
            is RootContext -> context.activeChild?.activeChild
        }

    @Deprecated("Use destinationContext or context instead for greater clarity about the context being used")
    public val navigationContext: AnyNavigationContext
        get() = context

    @Deprecated("Use instance")
    public val instruction: NavigationKey.Instance<K> = instance

    /**
     * The outcome the synthetic settled on. Null while the block is running and
     * before any method is called; set the first time one of the scope's
     * outcome methods runs (or by the dispatcher after the block falls through).
     * Read by the dispatcher; written via [setOutcome].
     */
    internal var outcome: SyntheticDestinationOutcome? = null
        private set

    /**
     * Records [newOutcome] and throws it. If an outcome is already set,
     * throws an [IllegalStateException] instead — this catches the case
     * where an async coroutine outlived the block and tried to complete /
     * close the synthetic after the dispatcher already moved on.
     */
    internal fun setOutcome(newOutcome: SyntheticDestinationOutcome): Nothing {
        val current = outcome
        if (current != null) {
            error(
                "SyntheticDestination for ${instance.key} has already finished with " +
                    "${current::class.simpleName}. A second outcome cannot be set — this " +
                    "usually means an async coroutine outlived the synthetic block and " +
                    "tried to complete/close it after the dispatcher had already moved on. " +
                    "Do any async work before opening the synthetic, or forward to a " +
                    "destination that owns the work itself."
            )
        }
        outcome = newOutcome
        throw newOutcome
    }

    /**
     * Used by the dispatcher when the block falls through without calling an
     * outcome method. Records a silent close so that any later coroutine
     * call sees [outcome] is non-null and throws the "already finished"
     * error from [setOutcome].
     */
    internal fun finalizeAsSilentCloseIfNoOutcome(): SyntheticDestinationOutcome {
        val existing = outcome
        if (existing != null) return existing
        val silent = SyntheticDestinationOutcome.Close(silent = true)
        outcome = silent
        return silent
    }

    /**
     * End the synthetic's outcome decision by opening another [NavigationKey]
     * in place of this synthetic. The synthetic instance itself never lands
     * in any backstack.
     */
    public fun open(key: NavigationKey): Nothing =
        setOutcome(SyntheticDestinationOutcome.Open(key.asInstance()))

    public fun open(key: NavigationKey.WithMetadata<*>): Nothing =
        setOutcome(SyntheticDestinationOutcome.Open(key.asInstance()))

    /**
     * End the synthetic's outcome decision by registering a `Closed` result
     * against whichever [dev.enro.result.NavigationResultChannel] originally
     * opened this synthetic.
     */
    public fun close(): Nothing =
        setOutcome(SyntheticDestinationOutcome.Close(silent = false))

    /**
     * Close the synthetic without firing the result-channel callback. The
     * caller's `onClosed` won't run. Use this when the synthetic acts as a
     * pure side-effect bridge and the original caller doesn't need to know
     * the synthetic finished.
     */
    public fun closeSilently(): Nothing =
        setOutcome(SyntheticDestinationOutcome.Close(silent = true))

    /**
     * End the synthetic's outcome decision by registering a `Completed`
     * result with no payload.
     *
     * For synthetics whose key is a [NavigationKey.WithResult], this no-arg
     * overload is shadowed by a deprecated-error extension — you must call
     * the typed `complete(result: T)` extension instead.
     */
    public fun complete(): Nothing =
        setOutcome(SyntheticDestinationOutcome.Complete(result = null))

    /**
     * End the synthetic's outcome decision by opening [key] and routing its
     * eventual completion back to whoever opened the synthetic. The
     * synthetic itself doesn't produce the result; the forwarded key does.
     *
     * For synthetics whose key is a [NavigationKey.WithResult], the
     * forwarded key must also be a [NavigationKey.WithResult] of the same
     * result type — that variant is provided as an extension; this non-result
     * overload is blocked on result-bearing scopes by a deprecated-error
     * extension.
     */
    public fun completeFrom(key: NavigationKey): Nothing =
        setOutcome(SyntheticDestinationOutcome.CompleteFrom(key.asInstance()))
}
