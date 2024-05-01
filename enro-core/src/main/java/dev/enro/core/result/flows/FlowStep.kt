package dev.enro.core.result.flows

import android.os.Parcelable
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationKey
import kotlinx.parcelize.Parcelize

public sealed interface FlowStepConfiguration : Parcelable{
    @Parcelize
    public data object Transient : FlowStepConfiguration
}

@Parcelize
public class FlowStep<Result: Any> private constructor(
    @PublishedApi internal val stepId: String,
    @PublishedApi internal val key: NavigationKey,
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
        dependsOn = dependsOn.contentHash(),
        direction = direction,
        configuration = configuration,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

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

private fun List<Any?>.contentHash(): Long = fold(0L) { result, it -> 31L * result + it.hashCode() }

internal val FlowStep<*>.isTransient: Boolean
    get() = configuration.contains(FlowStepConfiguration.Transient)
