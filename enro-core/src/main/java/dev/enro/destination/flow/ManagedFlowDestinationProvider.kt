package dev.enro.destination.flow

import dev.enro.core.NavigationKey
import dev.enro.core.TypedNavigationHandle
import dev.enro.core.result.flows.NavigationFlowScope

public class ManagedFlowDestinationProvider<Key : NavigationKey, Result>(
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

public class ManagedFlowDestinationScope<T : NavigationKey>(
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

public class ManagedFlowCompleteScope<T : NavigationKey>(
    public val navigation: TypedNavigationHandle<T>,
)

public class ManagedFlowDestinationBuilder {
    public class NeedsFlow<Key : NavigationKey> {
        public fun <Result> flow(flow: ManagedFlowDestinationScope<Key>.() -> Result): NeedsOnComplete<Key, Result> {
            return NeedsOnComplete(flow)
        }
    }

    public class NeedsOnComplete<Key : NavigationKey, Result>(
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

public fun <T : NavigationKey> managedFlowDestination(): ManagedFlowDestinationBuilder.NeedsFlow<T> {
    return ManagedFlowDestinationBuilder.NeedsFlow()
}