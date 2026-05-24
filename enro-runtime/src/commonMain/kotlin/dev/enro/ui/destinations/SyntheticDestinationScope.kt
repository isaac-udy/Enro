package dev.enro.ui.destinations

import dev.enro.NavigationContext
import dev.enro.NavigationKey
import dev.enro.asInstance
import dev.enro.context.ContainerContext
import dev.enro.context.DestinationContext
import dev.enro.context.RootContext

/**
 * The receiver scope of a `syntheticDestination { ... }` block. A synthetic
 * destination is a [NavigationKey] that, when opened, runs the block instead
 * of rendering a UI; the synthetic instance never reaches a backstack.
 *
 * Outcomes split into **pure** outcomes (synchronous, in-place rewrite of
 * the original `Open(synthetic)`) and a **side-effect** outcome (deferred
 * imperative work with platform/container access). The pure outcomes are:
 *
 * - [open] — open another [NavigationKey] in place of the synthetic.
 * - [close] — register a `Closed` result for whoever called the synthetic.
 * - [closeSilently] — close without firing the result-channel callback.
 * - [complete] — register a `Completed` result. For result-bearing
 *   synthetics see the `complete(result: T)` extension.
 * - [completeFrom] — open another key and have *its* completion fulfil the
 *   synthetic's contract.
 *
 * Pure outcomes flow through the same interceptor pipeline as any other
 * operation, in order, in the same processing pass. That preserves
 * ordering when synthetics appear in an initial backstack alongside
 * normal destinations.
 *
 * For everything else — launching a system browser, rewriting the
 * container's backstack, calling out to a non-Enro API — reach for
 * [sideEffect]. The side-effect block runs deferred, has access to the
 * originating [NavigationContext] and the target [dev.enro.NavigationContainer],
 * and never blocks the operation pipeline.
 *
 * The block can fall through without calling any outcome method — that's
 * treated as a silent close (no result-channel callback fires, no
 * operation is dispatched against the backstack).
 *
 * Each outcome method throws a sentinel and returns [Nothing], so calls
 * inside conditionals flow naturally without needing explicit `return`.
 * The scope tracks the outcome it settles on: once one is set, any further
 * outcome call from an async coroutine that outlived the block throws a
 * clear "already finished" error rather than silently double-handling.
 */
public class SyntheticDestinationScope<K : NavigationKey> @PublishedApi internal constructor(
    /**
     * The [NavigationContext] the synthetic was opened from. Intended for
     * reads — inspecting `controller`, walking parent contexts, checking
     * `activeChild` — to inform the outcome decision. Imperative actions
     * (calling `controller.execute`, mutating containers) should go through
     * [sideEffect] instead, which is dispatched after the synthetic's own
     * outcome has settled.
     */
    public val context: NavigationContext,
    public val instance: NavigationKey.Instance<K>,
) {
    public val key: K = instance.key

    /**
     * The active destination closest to [context]. Read-only convenience —
     * useful for synthetics that want to know "what screen am I being opened
     * from?" Walks the context tree: if [context] is a
     * [DestinationContext] this is that context; if it's a
     * [ContainerContext], it's the container's active child; if it's a
     * [RootContext], it's the active child of the root's active container.
     */
    public val destinationContext: DestinationContext<NavigationKey>?
        get() = when (context) {
            is DestinationContext<*> -> context
            is ContainerContext -> context.activeChild
            is RootContext -> context.activeChild?.activeChild
        }

    /**
     * The outcome the synthetic settled on. Null while the block is running
     * and before any method is called; set the first time one of the scope's
     * outcome methods runs (or by the dispatcher after the block falls
     * through to record a silent close).
     */
    internal var outcome: SyntheticDestinationOutcome? = null
        private set

    /**
     * Records [newOutcome] and throws it. If an outcome is already set,
     * throws an [IllegalStateException] instead — this catches the case
     * where an async coroutine outlived the block and tried to complete /
     * close the synthetic after the dispatcher had already moved on.
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
     * in any backstack; the dispatcher rewrites the original `Open(synthetic)`
     * to `Open(key)` inline.
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
     * Close the synthetic without firing the result-channel callback. Use
     * when the synthetic acted as a pure side-effect bridge and the original
     * caller doesn't need to know the synthetic finished.
     */
    public fun closeSilently(): Nothing =
        setOutcome(SyntheticDestinationOutcome.Close(silent = true))

    /**
     * End the synthetic's outcome decision by registering a `Completed`
     * result with no payload.
     *
     * For synthetics whose key is a [NavigationKey.WithResult], this no-arg
     * overload is shadowed by a deprecated-error extension — call the typed
     * `complete(result: T)` extension instead.
     */
    public fun complete(): Nothing =
        setOutcome(SyntheticDestinationOutcome.Complete(result = null))

    /**
     * End the synthetic's outcome decision by opening [key] and routing its
     * eventual completion back to whoever opened the synthetic. The
     * synthetic itself doesn't produce the result; the forwarded key does.
     */
    public fun completeFrom(key: NavigationKey): Nothing =
        setOutcome(SyntheticDestinationOutcome.CompleteFrom(key.asInstance()))

    /**
     * End the synthetic's outcome decision by dispatching a side effect.
     * The [block] runs deferred, in `afterExecution` of the current
     * operation pass — meaning every other operation in the same pass has
     * already settled by the time the block runs. Use this when the
     * synthetic needs platform handles (e.g. an Android Activity), the
     * container reference, or any imperative work that doesn't fit a single
     * navigation operation.
     *
     * The side-effect block runs with a [SyntheticSideEffectScope] receiver
     * carrying `context`, `container`, `instance`, and `key`. From inside
     * the block you can call `container.execute(context, ...)` to drive
     * further navigation (including `SetBackstack` for whole-backstack
     * rewrites). The synthetic itself is treated as silently closed once
     * the side effect dispatches — no result-channel callback fires.
     */
    public fun sideEffect(
        block: SyntheticSideEffectScope<K>.() -> Unit,
    ): Nothing {
        @Suppress("UNCHECKED_CAST")
        setOutcome(
            SyntheticDestinationOutcome.SideEffect(
                block as SyntheticSideEffectScope<NavigationKey>.() -> Unit,
            )
        )
    }
}
