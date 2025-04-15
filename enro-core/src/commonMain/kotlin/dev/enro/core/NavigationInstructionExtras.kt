package dev.enro.core

import androidx.savedstate.serialization.SavedStateConfiguration
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import androidx.savedstate.serialization.serializers.SavedStateSerializer
import dev.enro.core.controller.NavigationController
import dev.enro.core.internal.isDebugBuild
import kotlinx.serialization.Contextual
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
        override val descriptor: SerialDescriptor = SavedStateSerializer.descriptor

        override fun serialize(encoder: Encoder, value: NavigationInstructionExtras) {
            val config = SavedStateConfiguration {
                serializersModule = NavigationController.serializersModule
            }
            val serializer = MapSerializer(String.serializer(), PolymorphicSerializer(Any::class))
            val savedState = encodeToSavedState(serializer, value.map, config)
            SavedStateSerializer.serialize(encoder, savedState)
        }

        override fun deserialize(decoder: Decoder): NavigationInstructionExtras {
            val config = SavedStateConfiguration { serializersModule = NavigationController.serializersModule }
            val savedState = SavedStateSerializer.deserialize(decoder)
            val serializer = MapSerializer(String.serializer(), PolymorphicSerializer(Any::class))
            val map = decodeFromSavedState(serializer, savedState, config)
            return NavigationInstructionExtras(map.toMutableMap())
        }
    }

    public val values: Map<String, Any>
        get() = map

    public fun <T : Any> get(key: String): T? {
        return map[key] as? T?
    }

    public fun remove(key: String) {
        map.remove(key)
    }

    public inline fun <reified T : Any> put(key: String, value: T) {
        if (isDebugBuild()) {
            val hasSerializer = NavigationController.serializersModule.getPolymorphic(Any::class, value) != null
            if (!hasSerializer) {
                error("Object of type ${T::class} could not be added to NavigationInstructionExtras. Make sure to register the serializer with the NavigationController.")
            }
        }
        map.put(key, value)
    }

    public fun putAll(other: NavigationInstructionExtras) {
        map.putAll(other.map)
    }
}
