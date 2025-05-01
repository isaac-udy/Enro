package dev.enro.core.result.flows

import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationInstructionExtras
import dev.enro.core.NavigationKey
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
public sealed interface FlowStepConfiguration {
    @Serializable
    public data object Transient : FlowStepConfiguration
}

@Serializable
public class FlowStep<Result : Any> private constructor(
    @PublishedApi internal val stepId: String,
    @PublishedApi internal val key: @Contextual NavigationKey,
    @PublishedApi internal val extras: NavigationInstructionExtras,
    @PublishedApi internal val dependsOn: Long,
    @PublishedApi internal val direction: NavigationDirection,
    @PublishedApi internal val configuration: Set<FlowStepConfiguration>,
) : NavigationKey.SupportsPush.WithResult<Result>,
    NavigationKey.SupportsPresent.WithResult<Result> {

    internal constructor(
        stepId: String,
        key: NavigationKey,
        dependsOn: List<Any?>,
        direction: NavigationDirection,
        configuration: Set<FlowStepConfiguration>,
    ) : this(
        stepId = stepId,
        key = key,
        extras = NavigationInstructionExtras(),
        dependsOn = dependsOn.hashForDependsOn(),
        direction = direction,
        configuration = configuration,
    )

    internal constructor(
        stepId: String,
        key: NavigationKey.WithExtras<out NavigationKey>,
        dependsOn: List<Any?>,
        direction: NavigationDirection,
        configuration: Set<FlowStepConfiguration>,
    ) : this(
        stepId = stepId,
        key = key.navigationKey,
        extras = key.extras,
        dependsOn = dependsOn.hashForDependsOn(),
        direction = direction,
        configuration = configuration,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as FlowStep<*>

        if (stepId != other.stepId) return false
        if (key != other.key) return false
        if (dependsOn != other.dependsOn) return false
        if (direction != other.direction) return false
        if (configuration != other.configuration) return false

        return true
    }

    override fun hashCode(): Int {
        var result = stepId.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + dependsOn.hashCode()
        result = 31 * result + direction.hashCode()
        result = 31 * result + configuration.hashCode()
        return result
    }
}

internal val FlowStep<*>.isTransient: Boolean
    get() = configuration.contains(FlowStepConfiguration.Transient)
