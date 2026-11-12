package dev.enro.ui.destinations

import dev.enro.NavigationContext
import dev.enro.NavigationKey
import dev.enro.asInstance
import dev.enro.context.AnyNavigationContext
import dev.enro.context.ContainerContext
import dev.enro.context.DestinationContext
import dev.enro.context.RootContext

/**
 * The receiver of a `syntheticDestination { ... }` block. Opening a synthetic destination's
 * [NavigationKey] runs the block and renders no UI. The synthetic's instance is never added to
 * a backstack.
 *
 * The block selects one outcome by calling an outcome method:
 *
 * - [open] opens another [NavigationKey] in place of the synthetic.
 * - [close] registers a `Closed` result for the caller that opened the synthetic.
 * - [closeSilently] closes the synthetic without calling the result-channel callback.
 * - [complete] registers a `Completed` result. A synthetic for a [NavigationKey.WithResult]
 *   calls the `complete(result)` extension.
 * - [completeFrom] opens another key and delivers the result of that key to the caller that
 *   opened the synthetic.
 * - [sideEffect] runs a block after the container has applied the other operations.
 *
 * Every outcome except [sideEffect] replaces the synthetic's `Open` at the same position in
 * the list of operations, so a synthetic in an initial backstack keeps its position.
 *
 * A block that returns without calling an outcome method is a silent close.
 *
 * Each outcome method throws and returns [Nothing], so the block ends at the first outcome
 * method called. An outcome method called after an outcome is set, for example from a
 * coroutine that is still running after the block returned, throws an
 * [IllegalStateException].
 */
public class SyntheticDestinationScope<K : NavigationKey> @PublishedApi internal constructor(
    /**
     * The [NavigationContext] that opened the synthetic. Read [context] to select an outcome.
     * To execute navigation operations or change a container, use [sideEffect], which runs
     * after the synthetic's outcome is applied.
     */
    public val context: NavigationContext,
    public val instance: NavigationKey.Instance<K>,
) {
    public val key: K = instance.key

    /**
     * The destination that the synthetic was opened from: [context] when [context] is a
     * [DestinationContext], the active child of a [ContainerContext], or the active child of
     * the active container of a [RootContext]. `null` when the container or root has no
     * active child.
     */
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

    /** `null` until an outcome method or [finalizeAsSilentCloseIfNoOutcome] sets the outcome. */
    internal var outcome: SyntheticDestinationOutcome? = null
        private set

    /**
     * Records [newOutcome] and throws [newOutcome]. Throws an [IllegalStateException] when an
     * outcome is already set.
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
     * Records a silent close when the block returned without calling an outcome method, so
     * that a later outcome method call throws from [setOutcome].
     */
    internal fun finalizeAsSilentCloseIfNoOutcome(): SyntheticDestinationOutcome {
        val existing = outcome
        if (existing != null) return existing
        val silent = SyntheticDestinationOutcome.Close(silent = true)
        outcome = silent
        return silent
    }

    /**
     * Ends the synthetic's block and opens [key] in place of the synthetic. `Open(key)`
     * replaces the synthetic's `Open` at the same position in the list of operations.
     */
    public fun open(key: NavigationKey): Nothing =
        setOutcome(SyntheticDestinationOutcome.Open(key.asInstance()))

    public fun open(key: NavigationKey.WithMetadata<*>): Nothing =
        setOutcome(SyntheticDestinationOutcome.Open(key.asInstance()))

    /**
     * Ends the synthetic's block and registers a `Closed` result with the
     * [dev.enro.result.NavigationResultChannel] that opened the synthetic.
     */
    public fun close(): Nothing =
        setOutcome(SyntheticDestinationOutcome.Close(silent = false))

    /**
     * Ends the synthetic's block and closes the synthetic without calling the result-channel
     * callback.
     */
    public fun closeSilently(): Nothing =
        setOutcome(SyntheticDestinationOutcome.Close(silent = true))

    /**
     * Ends the synthetic's block and registers a `Completed` result with no value.
     *
     * A synthetic whose key is a [NavigationKey.WithResult] calls the `complete(result)`
     * extension. A no-argument `complete()` extension with `DeprecationLevel.ERROR` is declared
     * for such a synthetic.
     */
    public fun complete(): Nothing =
        setOutcome(SyntheticDestinationOutcome.Complete(result = null))

    /**
     * Ends the synthetic's block and opens [key]. The completion of [key] is delivered to the
     * caller that opened the synthetic.
     */
    public fun completeFrom(key: NavigationKey): Nothing =
        setOutcome(SyntheticDestinationOutcome.CompleteFrom(key.asInstance()))

    /**
     * Ends the synthetic's block and schedules [block]. [block] runs after the container has
     * applied every operation that was executed together with the synthetic's `Open`. Use
     * [sideEffect] for work that is not a single navigation operation, for example starting an
     * Android Activity or replacing the container's backstack with
     * `container.execute(context, ...)`.
     *
     * No result-channel callback is called for the synthetic.
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
