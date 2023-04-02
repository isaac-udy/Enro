package dev.enro.core.result.flows

import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationKey
import kotlinx.parcelize.Parcelize

@Parcelize
public data class FlowStep<Result: Any> private constructor(
    val stepId: String,
    val key: NavigationKey,
    val dependsOn: Long,
    val direction: NavigationDirection,
) : NavigationKey.SupportsPush.WithResult<Result>,
    NavigationKey.SupportsPresent.WithResult<Result> {

    internal constructor(
        stepId: String,
        key: NavigationKey,
        dependsOn: List<Any?>,
        direction: NavigationDirection,
    ) : this(
        stepId = stepId,
        key = key,
        dependsOn = dependsOn.contentHash(),
        direction = direction,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FlowStep<*>

        if (stepId != other.stepId) return false
        if (key != other.key) return false
        if (dependsOn != other.dependsOn) return false
        if (direction != other.direction) return false

        return true
    }

    override fun hashCode(): Int {
        var result = stepId.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + dependsOn.hashCode()
        result = 31 * result + direction.hashCode()
        return result
    }

}

private fun List<Any?>.contentHash(): Long = fold(0L) { result, it -> 31L * result + it.hashCode() }
