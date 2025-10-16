package dev.enro.result.flow

import kotlinx.serialization.Serializable

@Serializable
public sealed interface FlowStepOptions {
    @Serializable
    public data object Transient : FlowStepOptions

    @Serializable
    public data object AlwaysAfterPrevious : FlowStepOptions
}


internal val FlowStep<*>.isTransient: Boolean
    get() = options.contains(FlowStepOptions.Transient)
