package dev.enro.core

import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlin.reflect.KClass

public abstract class NavigationKeySerializer<T : NavigationKey>(
    keyClass: KClass<T>,
) {
    public abstract fun serialize(key: T): String
    public abstract fun deserialize(data: String): T

    init {
        @Suppress("LeakingThis")
        register(keyClass, this)
    }

    public companion object {
        private val serializers: MutableMap<String, NavigationKeySerializer<out NavigationKey>> = mutableMapOf()

        public fun <T : NavigationKey> register(keyClass: KClass<T>, serializer: NavigationKeySerializer<T>) {
            val qualifiedName = keyClass.qualifiedName
                ?: error("NavigationKeys must have a qualified name - local and anonymous classes are not supported")

            serializers[qualifiedName] = serializer
        }

        public inline fun <reified T: NavigationKey> register(serializer: NavigationKeySerializer<T>) {
            register(T::class, serializer)
        }

        internal fun serialize(key: NavigationKey): String {
            val qualifiedName = key::class.qualifiedName
                ?: error("NavigationKeys must have a qualified name - local and anonymous classes are not supported")

            val serializer = serializers[qualifiedName]
                ?: error("No NavigationKeySerializer found for key type $qualifiedName")

            @Suppress("UNCHECKED_CAST")
            serializer as NavigationKeySerializer<NavigationKey>

            val serialized = serializer.serialize(key)
            return "${key::class.qualifiedName}|$serialized"
        }

        internal fun deserialize(data: String): NavigationKey {
            val (keyType, serialized) = data.split('|', limit = 2)
            val serializer = serializers[keyType] ?: error("No NavigationKeySerializer found for key type $keyType")
            return serializer.deserialize(serialized)
        }
    }

    public object KSerializer : kotlinx.serialization.KSerializer<NavigationKey> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("NavigationKey") {
            element("keyType", String.serializer().descriptor)
            element("serialized", String.serializer().descriptor)
        }

        override fun deserialize(decoder: Decoder): NavigationKey {
            lateinit var keyType: String
            lateinit var serialized: String

            decoder.decodeStructure(descriptor) {
                while (true) {
                    when (val index = decodeElementIndex(descriptor)) {
                        0 -> keyType = decodeStringElement(descriptor, 0)
                        1 -> serialized = decodeStringElement(descriptor, 1)
                        DECODE_DONE -> break // Input is over
                        else -> error("Unexpected index: $index")
                    }
                }
            }

            return serializers[keyType]?.deserialize(serialized)
                ?: error("No NavigationKeySerializer found for key type $keyType")
        }

        override fun serialize(encoder: Encoder, value: NavigationKey) {
            encoder.encodeStructure(descriptor) {
                val qualifiedName = value::class.qualifiedName
                    ?: error("NavigationKeys must have a qualified name - local and anonymous classes are not supported")

                val serializer = serializers[qualifiedName]
                    ?: error("No NavigationKeySerializer found for key type $qualifiedName")

                @Suppress("UNCHECKED_CAST")
                serializer as NavigationKeySerializer<NavigationKey>

                encodeStringElement(descriptor, 0, qualifiedName)
                encodeStringElement(descriptor, 1, serializer.serialize(value))
            }
        }
    }
}
