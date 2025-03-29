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
    @Deprecated("Please use Push or Present")
    public data object Forward : NavigationDirection()

    @Deprecated("Please use a Push or Present followed by a close")
    public data object Replace : NavigationDirection()

    public data object Push : NavigationDirection()

    public data object Present : NavigationDirection()

    public data object ReplaceRoot : NavigationDirection()

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
                "forward" -> Forward
                "replace" -> Replace
                "push" -> Push
                "present" -> Present
                "replaceRoot" -> ReplaceRoot
                else -> error("Unknown NavigationDirection: $direction")
            }
        }

        override fun serialize(encoder: Encoder, value: NavigationDirection) {
            encoder.encodeStructure(descriptor) {
                encodeStringElement(descriptor, 0, when (value) {
                    is Forward -> "forward"
                    is Replace -> "replace"
                    is Push -> "push"
                    is Present -> "present"
                    is ReplaceRoot -> "replaceRoot"
                })
            }
        }
    }
}
