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
 * to Chrome Custom Tabs) and Enro takes no further navigation action.
 *
 * Alternatively, the block can short-circuit by calling one of the outcome
 * methods:
 *
 * - [open] — open another [NavigationKey] instead of the synthetic.
 * - [close] — register a `Closed` result for whoever called the synthetic.
 * - [complete] — register a `Completed` result. For result-bearing
 *   synthetics, see the `complete(result: T)` extension.
 * - [completeFrom] — open another key and have *its* completion fulfil the
 *   synthetic's contract. Useful for "decider" synthetics that pick one of
 *   several destinations at runtime.
 *
 * Each outcome method throws a sentinel and returns [Nothing], so calls
 * inside conditionals flow naturally without needing explicit `return`.
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
     * End the synthetic's outcome decision by opening another [NavigationKey]
     * in place of this synthetic. The synthetic instance itself never lands
     * in any backstack.
     */
    public fun open(key: NavigationKey): Nothing =
        throw SyntheticDestinationOutcome.Open(key.asInstance())

    public fun open(key: NavigationKey.WithMetadata<*>): Nothing =
        throw SyntheticDestinationOutcome.Open(key.asInstance())

    /**
     * End the synthetic's outcome decision by registering a `Closed` result
     * against whichever [dev.enro.result.NavigationResultChannel] originally
     * opened this synthetic.
     */
    public fun close(): Nothing = throw SyntheticDestinationOutcome.Close()

    /**
     * End the synthetic's outcome decision by registering a `Completed`
     * result with no payload.
     *
     * For synthetics whose key is a [NavigationKey.WithResult], this no-arg
     * overload is shadowed by a deprecated-error extension — you must call
     * the typed `complete(result: T)` extension instead.
     */
    public fun complete(): Nothing = throw SyntheticDestinationOutcome.Complete(result = null)

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
        throw SyntheticDestinationOutcome.CompleteFrom(key.asInstance())
}
