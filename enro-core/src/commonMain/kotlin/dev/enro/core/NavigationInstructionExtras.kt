package dev.enro.core

import androidx.savedstate.read
import androidx.savedstate.savedState
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import androidx.savedstate.serialization.serializers.SavedStateSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer

@Serializable(with = NavigationInstructionExtras.Serializer::class)
public class NavigationInstructionExtras internal constructor() {
    private val map: MutableMap<String, Any?> = mutableMapOf()
    private val serializers: MutableMap<String, KSerializer<*>> = mutableMapOf()

    public object Serializer : KSerializer<NavigationInstructionExtras> {
        override val descriptor: SerialDescriptor = SavedStateSerializer.descriptor

        override fun serialize(encoder: Encoder, value: NavigationInstructionExtras) {
            val savedState = savedState {
                value.map.forEach { (k, v) ->
                    if (v == null) return@forEach
                    val serializer = value.serializers[k] ?: return@forEach
                    putSavedState(k, encodeToSavedState(serializer as KSerializer<Any>, v))
                    putString(k + "_serializer", serializerName(serializer))
                }
                putStringArray("keys", value.map.keys.toTypedArray())
            }
            SavedStateSerializer.serialize(encoder, savedState)
        }

        override fun deserialize(decoder: Decoder): NavigationInstructionExtras {
            val savedState = SavedStateSerializer.deserialize(decoder)
            val extras = NavigationInstructionExtras()
            val keys = savedState.read { getStringArrayOrNull("keys") }.orEmpty()
            keys.forEach {
                val serializerName = savedState.read { getStringOrNull(it + "_serializer") }
                val serializer = serializerName?.let { getSerializer(it) }
                if (serializer != null) {
                    val value = savedState.read { getSavedStateOrNull(it) }
                    if (value != null) {
                        extras.put(it, serializer, decodeFromSavedState(serializer, value))
                    }
                }
            }
            return extras
        }
    }

    public val values: Map<String, Any?>
        get() = map

    public fun <T : Any> get(key: String): T? {
        return map[key] as? T?
    }

    public fun remove(key: String) {
        map.remove(key)
        serializers.remove(key)
    }

    public fun <T : Any> put(key: String, serializer: KSerializer<T>, value: T) {
        map[key] = value
        serializers[key] = serializer
    }

    public inline fun <reified T : Any> put(key: String, value: T) {
        put(key, serializer<T>(), value)
    }

    public fun putAll(other: NavigationInstructionExtras) {
        map.putAll(other.map)
        serializers.putAll(other.serializers)
    }
}

// TODO: Need to change this to use a serializer module somewhere, but unsure where that would be registered
internal expect fun getSerializer(name: String) : KSerializer<Any>?
internal expect fun serializerName(serializer: KSerializer<Any>) : String