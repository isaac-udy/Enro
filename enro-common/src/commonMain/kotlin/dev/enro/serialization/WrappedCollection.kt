package dev.enro.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

@Serializable
internal sealed class WrappedCollection

@Serializable(with = WrappedList.Serializer::class)
@SerialName("WrappedList")
internal class WrappedList(val value: MutableList<Any?>) : WrappedCollection() {
    object Serializer : KSerializer<WrappedList> {
        private val innerSerializer = ListSerializer(PolymorphicSerializer(Any::class))
        override val descriptor = buildClassSerialDescriptor("WrappedList") {
            element("value", innerSerializer.descriptor)
        }

        override fun serialize(encoder: Encoder, value: WrappedList) {
            encoder.encodeStructure(descriptor) {
                encodeSerializableElement(
                    descriptor = descriptor,
                    index = 0,
                    serializer = innerSerializer,
                    value = value.value
                        .map { it.internalWrapForSerialization() },
                )
            }
        }

        override fun deserialize(decoder: Decoder): WrappedList {
            val list = decoder
                .decodeStructure(descriptor) {
                    decodeSerializableElement(
                        descriptor = descriptor,
                        index = decodeElementIndex(descriptor),
                        deserializer = innerSerializer,
                    )
                }
                .map { it.internalUnwrapForSerialization() }
            return WrappedList(list.toMutableList())
        }
    }
}

@Serializable(with = WrappedSet.Serializer::class)
@SerialName("WrappedSet")
internal class WrappedSet(val value: MutableSet<Any?>) : WrappedCollection() {
    object Serializer : KSerializer<WrappedSet> {
        private val innerSerializer = ListSerializer(PolymorphicSerializer(Any::class))
        override val descriptor = buildClassSerialDescriptor("WrappedSet") {
            element("value", innerSerializer.descriptor)
        }

        override fun serialize(encoder: Encoder, value: WrappedSet) {
            encoder.encodeStructure(descriptor) {
                encodeSerializableElement(
                    descriptor = descriptor,
                    index = 0,
                    serializer = innerSerializer,
                    value = value.value
                        .map { it.internalWrapForSerialization() },
                )
            }
        }

        override fun deserialize(decoder: Decoder): WrappedSet {
            val list = decoder
                .decodeStructure(descriptor) {
                    decodeSerializableElement(
                        descriptor = descriptor,
                        index = decodeElementIndex(descriptor),
                        deserializer = innerSerializer,
                    )
                }
                .map { it.internalUnwrapForSerialization() }
            return WrappedSet(list.toMutableSet())
        }
    }
}

@Serializable(with = WrappedMap.Serializer::class)
@SerialName("WrappedMap")
internal class WrappedMap(val value: MutableMap<Any, Any?>) : WrappedCollection() {
    object Serializer : KSerializer<WrappedMap> {
        private val innerSerializer = MapSerializer(PolymorphicSerializer(Any::class), PolymorphicSerializer(Any::class))
        override val descriptor = buildClassSerialDescriptor("WrappedMap") {
            element("value", innerSerializer.descriptor)
        }

        override fun serialize(encoder: Encoder, value: WrappedMap) {
            encoder.encodeStructure(descriptor) {
                encodeSerializableElement(
                    descriptor = descriptor,
                    index = 0,
                    serializer = innerSerializer,
                    value = value.value
                        .map { (key, value) ->
                            key.internalWrapForSerialization() to value.internalWrapForSerialization()
                        }
                        .toMap(),
                )
            }
        }

        override fun deserialize(decoder: Decoder): WrappedMap {
            val list = decoder
                .decodeStructure(descriptor) {
                    decodeSerializableElement(
                        descriptor = descriptor,
                        index = decodeElementIndex(descriptor),
                        deserializer = innerSerializer,
                    )
                }
                .map { (key, value) ->
                    key.internalUnwrapForSerialization() to value.internalUnwrapForSerialization()
                }
                .toMap()
            return WrappedMap(list.toMutableMap())
        }
    }
}