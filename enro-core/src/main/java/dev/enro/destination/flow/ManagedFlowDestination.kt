package dev.enro.destination.flow

import dev.enro.core.NavigationKey
import dev.enro.core.result.flows.NavigationFlowScope

public abstract class ManagedFlowDestination<Key : NavigationKey, Result> internal constructor() {
    internal abstract fun NavigationFlowScope.flow(): Result
    internal abstract fun onCompleted(result: Result)
}