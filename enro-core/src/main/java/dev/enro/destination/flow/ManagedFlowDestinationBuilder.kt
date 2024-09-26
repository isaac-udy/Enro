package dev.enro.destination.flow

import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.core.NavigationKey
import dev.enro.core.TypedNavigationHandle
import dev.enro.core.result.flows.NavigationFlowScope

/**
 * A [ManagedFlowDestinationBuilder] is used to build a [ManagedFlowDestinationProvider].
 *
 * To create a [ManagedFlowDestinationProvider], use the [managedFlowDestination] function, then call
 * [ManagedFlowDestinationBuilder.NeedsFlow.flow] to define the flow for the destination, then call
 * [ManagedFlowDestinationBuilder.NeedsOnComplete.onComplete] to define the completion block for the destination,
 * which will return a [ManagedFlowDestinationProvider] which can be bound as a navigation destination.
 */
@ExperimentalEnroApi
public class ManagedFlowDestinationBuilder internal constructor() {

    /**
     * A [NeedsFlow] is a class that is used to define the flow for a [ManagedFlowDestinationProvider] using [managedFlowDestination].
     * It provides a [flow] function that takes a lambda that defines the flow for the destination, and returns a
     * [NeedsOnComplete] that can be used to define the completion block for the destination.
     */
    @ExperimentalEnroApi
    public class NeedsFlow<Key : NavigationKey> internal constructor() {
        public fun <Result> flow(flow: ManagedFlowDestinationScope<Key>.() -> Result): NeedsOnComplete<Key, Result> {
            return NeedsOnComplete(flow)
        }
    }

    /**
     * A [NeedsOnComplete] is a class that is used to define the completion block for a [ManagedFlowDestinationProvider] using
     * [managedFlowDestination]. It provides an [onComplete] function that takes a lambda that defines the completion block for
     * the destination, and returns a [ManagedFlowDestinationProvider] that can be bound as a navigation destination.
     */
    @ExperimentalEnroApi
    public class NeedsOnComplete<Key : NavigationKey, Result> internal constructor(
        private val flow: ManagedFlowDestinationScope<Key>.() -> Result,
    ) {
        public fun onComplete(onComplete: ManagedFlowCompleteScope<Key>.(Result) -> Unit): ManagedFlowDestinationProvider<Key, Result> {
            return ManagedFlowDestinationProvider(
                flow = flow,
                onCompleted = onComplete,
            )
        }
    }
}

/**
 * A [ManagedFlowDestinationScope] is an extension of [NavigationFlowScope] that is used when building a [ManagedFlowDestination]
 * using [managedFlowDestination] and [ManagedFlowDestinationBuilder.NeedsFlow]. It provides access to a [TypedNavigationHandle]
 * for the destination, and provides all the same functionality as a [NavigationFlowScope].
 */
@ExperimentalEnroApi
public class ManagedFlowDestinationScope<T : NavigationKey> internal constructor(
    delegate: NavigationFlowScope,
    public val navigation: TypedNavigationHandle<T>,
) : NavigationFlowScope(
    flow = delegate.flow,
    coroutineScope = delegate.coroutineScope,
    resultManager = delegate.resultManager,
    navigationFlowReference = delegate.navigationFlowReference,
    steps = delegate.steps,
    suspendingSteps = delegate.suspendingSteps,
)

/**
 * A [ManagedFlowCompleteScope] is a scope that is used to provide a [TypedNavigationHandle] for a [ManagedFlowDestination]
 * using [managedFlowDestination] and [ManagedFlowDestinationBuilder.NeedsOnComplete]. It provides access to a
 * [TypedNavigationHandle] for the destination.
 */
@ExperimentalEnroApi
public class ManagedFlowCompleteScope<T : NavigationKey> internal constructor(
    public val navigation: TypedNavigationHandle<T>,
)

/**
 * [managedFlowDestination] is used to create a [ManagedFlowDestinationProvider]/[ManagedFlowDestination] to be bound to a
 * [NavigationKey].
 *
 * This function returns a [ManagedFlowDestinationBuilder.NeedsFlow]. By calling [ManagedFlowDestinationBuilder.NeedsFlow.flow]
 * on this object, you can define the flow for the destination. Calling flow will return a
 * [ManagedFlowDestinationBuilder.NeedsOnComplete], and by calling [ManagedFlowDestinationBuilder.NeedsOnComplete.onComplete],
 * you are able to provide a completion block for the managed flow destination.
 * [ManagedFlowDestination.NeedsOnComplete.onComplete] will return a [ManagedFlowDestinationProvider] that can be
 * bound as a navigation destination.
 *
 * Example:
 * ```
 * @NavigationDestination(ExampleKey::class)
 * val exampleDestination = managedFlowDestination<ExampleKey>()
 *     .flow { ... } // define the flow
 *     .onComplete { ... } // define the completion block
 * ```
 */
@ExperimentalEnroApi
public fun <T : NavigationKey> managedFlowDestination(): ManagedFlowDestinationBuilder.NeedsFlow<T> {
    return ManagedFlowDestinationBuilder.NeedsFlow()
}