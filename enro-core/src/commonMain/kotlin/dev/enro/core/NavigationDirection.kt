package dev.enro.core

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

@Serializable(with = NavigationDirection.Serializer::class)
public sealed class NavigationDirection {
    @Serializable(with = NavigationDirection.Serializer::class)
    public data object Push : NavigationDirection()

    @Serializable(with = NavigationDirection.Serializer::class)
    public data object Present : NavigationDirection()

    public companion object {}

    public object Serializer : KSerializer<NavigationDirection> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("navigationDirection") {
            element("direction", String.serializer().descriptor)
        }

        override fun deserialize(decoder: Decoder): NavigationDirection {
            lateinit var direction: String

            decoder.decodeStructure(descriptor) {
                val index = decodeElementIndex(descriptor)
                if (index == 0) {
                    direction = decodeStringElement(descriptor, index)
                } else {
                    error("Unexpected index $index")
                }
            }
            return when (direction) {
                "push" -> Push
                "present" -> Present
                else -> error("Unknown NavigationDirection: $direction")
            }
        }

        override fun serialize(encoder: Encoder, value: NavigationDirection) {
            encoder.encodeStructure(descriptor) {
                encodeStringElement(
                    descriptor, 0, when (value) {
                        is Push -> "push"
                        is Present -> "present"
                    }
                )
            }
        }
    }
}
