package dev.enro.core

import dev.enro.core.controller.NavigationController
import dev.enro.core.internal.isDebugBuild
import dev.enro.core.serialization.unwrapForSerialization
import dev.enro.core.serialization.wrapForSerialization
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = NavigationInstructionExtras.Serializer::class)
public class NavigationInstructionExtras internal constructor(
    @PublishedApi
    internal val map: MutableMap<String, Any> = mutableMapOf()
) {
    public object Serializer : KSerializer<NavigationInstructionExtras> {
        private val innerSerializer = MapSerializer(String.serializer(), PolymorphicSerializer(Any::class))
        override val descriptor: SerialDescriptor = innerSerializer.descriptor

        override fun serialize(encoder: Encoder, value: NavigationInstructionExtras) {
            innerSerializer.serialize(
                encoder = encoder,
                value = value.map.mapValues {
                    it.value.wrapForSerialization()
                },
            )
        }

        override fun deserialize(decoder: Decoder): NavigationInstructionExtras {
            val map = innerSerializer
                .deserialize(decoder)
                .mapValues { it.value.unwrapForSerialization() }
            return NavigationInstructionExtras(map.toMutableMap())
        }
    }

    public val values: Map<String, Any>
        get() = map

    public fun <T : Any> get(key: String): T? {
        if (!map.containsKey(key)) return null
        return runCatching { map[key] as? T? }.getOrNull()
    }

    public fun remove(key: String) {
        map.remove(key)
    }

    @OptIn(ExperimentalSerializationApi::class)
    public fun <T : Any> put(key: String, value: T) {
        if (isDebugBuild()) {
            val wrapped = value.wrapForSerialization()
            val hasSerializer = NavigationController.serializersModule.getPolymorphic(Any::class, wrapped) != null
            if (!hasSerializer) {
                error("Object of type ${value::class} could not be added to NavigationInstructionExtras. Make sure to register the serializer with the NavigationController.")
            }
        }
        map.put(key, value)
    }

    public fun putAll(other: NavigationInstructionExtras) {
        map.putAll(other.map)
    }

    override fun toString(): String {
        return map.toString()
    }
}