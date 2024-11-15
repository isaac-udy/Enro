package dev.enro.destination.flow

import dev.enro.core.NavigationKey
import dev.enro.core.TypedNavigationHandle
import dev.enro.core.result.flows.NavigationFlowScope

/**
 * A ManagedFlowDestinationProvider is a class that provides a [ManagedFlowDestination] for a specific [NavigationKey].
 *
 * To create a [ManagedFlowDestinationProvider], use the [managedFlowDestination] function.
 */
public class ManagedFlowDestinationProvider<Key : NavigationKey, Result> internal constructor(
    internal val flow: ManagedFlowDestinationScope<Key>.() -> Result,
    internal val onCompleted: ManagedFlowCompleteScope<Key>.(Result) -> Unit,
) {
    public fun create(navigation: TypedNavigationHandle<Key>): ManagedFlowDestination<Key, Result> {
        return object : ManagedFlowDestination<Key, Result>() {
            override fun NavigationFlowScope.flow(): Result {
                return flow(ManagedFlowDestinationScope(this, navigation))
            }

            override fun onCompleted(result: Result) {
                ManagedFlowCompleteScope(navigation).onCompleted(result)
            }
        }
    }
}
