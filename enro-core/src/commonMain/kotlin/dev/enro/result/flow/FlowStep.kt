package dev.enro.result.flow

import dev.enro.NavigationKey
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
public sealed interface FlowStepConfiguration {
    @Serializable
    public data object Transient : FlowStepConfiguration
}

@Serializable
@ConsistentCopyVisibility
public data class FlowStep<Result : Any> private constructor(
    @PublishedApi internal val stepId: String,
    @PublishedApi internal val key: @Contextual NavigationKey,
    @PublishedApi internal val metadata: NavigationKey.Metadata,
    @PublishedApi internal val dependsOn: Long,
    @PublishedApi internal val configuration: Set<FlowStepConfiguration>,
) : NavigationKey.WithResult<Result> {

    internal constructor(
        stepId: String,
        key: NavigationKey,
        dependsOn: List<Any?>,
        configuration: Set<FlowStepConfiguration>,
    ) : this(
        stepId = stepId,
        key = key,
        metadata = NavigationKey.Metadata(),
        dependsOn = dependsOn.hashForDependsOn(),
        configuration = configuration,
    )

    internal constructor(
        stepId: String,
        key: NavigationKey.WithMetadata<out NavigationKey>,
        dependsOn: List<Any?>,
        configuration: Set<FlowStepConfiguration>,
    ) : this(
        stepId = stepId,
        key = key.key,
        metadata = key.metadata,
        dependsOn = dependsOn.hashForDependsOn(),
        configuration = configuration,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as FlowStep<*>

        if (stepId != other.stepId) return false
        if (key != other.key) return false
        if (metadata != other.metadata) return false
        if (dependsOn != other.dependsOn) return false
        if (configuration != other.configuration) return false

        return true
    }

    override fun hashCode(): Int {
        var result = stepId.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + metadata.hashCode()
        result = 31 * result + dependsOn.hashCode()
        result = 31 * result + configuration.hashCode()
        return result
    }

    public companion object {
    }
    public object MetadataKey : NavigationKey.MetadataKey<FlowStep<*>?>(null) {}
}

internal val FlowStep<*>.isTransient: Boolean
    get() = configuration.contains(FlowStepConfiguration.Transient)
