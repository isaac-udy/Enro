package dev.enro.core

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlin.uuid.Uuid

@Serializable(with = NavigationContainerKey.Serializer::class)
public sealed class NavigationContainerKey {
    public abstract val name: String
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (this::class != other::class) return false

        other as NavigationContainerKey

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return "NavigationContainerKey($name)"
    }

    public class Dynamic internal constructor(
        override val name: String
    ) : NavigationContainerKey() {
        public constructor() : this("DynamicContainerKey(${Uuid.random()})")
    }

    public class FromName(
        override val name: String
    ) : NavigationContainerKey()

    public class FromId private constructor(
        public val id: Int,
        override val name: String
    ) : NavigationContainerKey() {
        public constructor(id: Int) : this(
            id = id,
            name = "FromId($id)"
        )
    }

    public object Serializer : KSerializer<NavigationContainerKey> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("NavigationContainerKey") {
            element("type", String.serializer().descriptor)
            element("name", String.serializer().descriptor)
            element("id", Int.serializer().descriptor)
        }

        override fun deserialize(decoder: Decoder): NavigationContainerKey {
            lateinit var type: String
            lateinit var name: String
            var id: Int? = null

            decoder.decodeStructure(descriptor) {
                while (true) {
                    when (val index = decodeElementIndex(descriptor)) {
                        0 -> type = decodeStringElement(descriptor, index)
                        1 -> name = decodeStringElement(descriptor, index)
                        2 -> id = decodeIntElement(descriptor, index)
                        CompositeDecoder.DECODE_DONE -> break
                        else -> throw SerializationException("Unknown index: $index")
                    }
                }
            }
            return when (type) {
                "Dynamic" -> Dynamic(name)
                "FromName" -> FromName(name)
                "FromId" -> FromId(id ?: throw SerializationException("Id is required for FromId"))
                else -> throw SerializationException("Unknown type: $type")
            }
        }

        override fun serialize(encoder: Encoder, value: NavigationContainerKey) {
            val type = when(value) {
                is Dynamic -> "Dynamic"
                is FromName -> "FromName"
                is FromId -> "FromId"
            }
            encoder.encodeStructure(descriptor) {
                encodeStringElement(descriptor, 0, type)
                encodeStringElement(descriptor, 1, value.name)

                when (value) {
                    is Dynamic -> encodeIntElement(descriptor, 2, -1)
                    is FromName -> encodeIntElement(descriptor, 2, -1)
                    is FromId -> encodeIntElement(descriptor, 2, value.id)
                }
            }
        }
    }
}