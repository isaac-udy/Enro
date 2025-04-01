package dev.enro.core

import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.savedState
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.reflect.KClass

public abstract class NavigationKeySerializer<T : NavigationKey>(
    keyClass: KClass<T>,
) {
    public abstract fun serialize(key: T): SavedState
    public abstract fun deserialize(data: SavedState): T

    init {
        @Suppress("LeakingThis")
        register(keyClass, this)
    }

    public companion object {
        internal val serializers: MutableMap<String, NavigationKeySerializer<out NavigationKey>> = mutableMapOf()
        internal val classes: MutableMap<String, KClass<out NavigationKey>> = mutableMapOf()

        public fun <T : NavigationKey> register(keyClass: KClass<T>, serializer: NavigationKeySerializer<T>) {
            val qualifiedName = keyClass.qualifiedName
                ?: error("NavigationKeys must have a qualified name - local and anonymous classes are not supported")

            serializers[qualifiedName] = serializer
            classes[qualifiedName] = keyClass
        }

        public inline fun <reified T: NavigationKey> register(serializer: NavigationKeySerializer<T>) {
            register(T::class, serializer)
        }

        internal fun serialize(key: NavigationKey): SavedState {
            val qualifiedName = key::class.qualifiedName
                ?: error("NavigationKeys must have a qualified name - local and anonymous classes are not supported")

            val serializer = serializers[qualifiedName]
                ?: error("No NavigationKeySerializer found for key type $qualifiedName")

            @Suppress("UNCHECKED_CAST")
            serializer as NavigationKeySerializer<NavigationKey>

            val serialized = serializer.serialize(key)
            return savedState {
                putString("keyType", qualifiedName)
                putSavedState("serialized", serialized)
            }
        }

        internal fun deserialize(data: SavedState): NavigationKey {
            return data.read {
                val keyType = getStringOrNull("keyType")
                    ?: error("No keyType found in saved state")

                val serializer = serializers[keyType] ?: error("No NavigationKeySerializer found for key type $keyType")

                val serialized = getSavedStateOrNull("serialized")
                    ?: error("No serialized data found in saved state")
                serializer.deserialize(serialized)
            }
        }
    }
}

public expect object NKSerializer : kotlinx.serialization.KSerializer<NavigationKey> {
    override val descriptor: SerialDescriptor
    override fun deserialize(decoder: Decoder): NavigationKey
    override fun serialize(encoder: Encoder, value: NavigationKey)
}