package dev.enro.result.flow

import dev.enro.NavigationKey
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
@ConsistentCopyVisibility
public data class FlowStep<out Result : Any> private constructor(
    @PublishedApi internal val id: Id<NavigationKey>,
    @PublishedApi internal val key: @Contextual NavigationKey,
    @PublishedApi internal val metadata: NavigationKey.Metadata,
    @PublishedApi internal val dependsOn: Long,
    @PublishedApi internal val options: Set<FlowStepOptions>,
) {
    internal constructor(
        id: Id<NavigationKey>,
        key: NavigationKey.WithMetadata<NavigationKey>,
        dependsOn: List<Any?>,
        options: Set<FlowStepOptions>,
    ) : this(
        id = id,
        key = key.key,
        metadata = key.metadata,
        dependsOn = dependsOn.hashForDependsOn(),
        options = options,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as FlowStep<*>

        if (id != other.id) return false
        if (key != other.key) return false
        if (metadata != other.metadata) return false
        if (dependsOn != other.dependsOn) return false
        if (options != other.options) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + metadata.hashCode()
        result = 31 * result + dependsOn.hashCode()
        result = 31 * result + options.hashCode()
        return result
    }

    public companion object {}

    @Serializable
    public class Id<out K : NavigationKey> @PublishedApi internal constructor(
        @PublishedApi internal val value: String,
    ) {
        internal object MetadataKey : NavigationKey.MetadataKey<String?>(null)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Id<*>

            return value == other.value
        }

        override fun hashCode(): Int {
            return value.hashCode()
        }

        override fun toString(): String {
            return "FlowStep.Id(value=$value)"
        }
    }
}

public val <T : NavigationKey> NavigationKey.Instance<T>.flowStepId: FlowStep.Id<T>?
    get() {
        val flowStepId = metadata.get(FlowStep.Id.MetadataKey) ?: return null
        return FlowStep.Id(flowStepId)
    }

public inline fun <reified T : NavigationKey> flowStepId(): FlowStep.Id<T> {
    val provider = object : FlowStepIdProvider<T> {
        override val type: KClass<T> = T::class
    }
    return FlowStep.Id(requireNotNull(provider::class.qualifiedName))
}

internal interface FlowStepIdProvider<T : NavigationKey> {
    val type: KClass<T>
}
