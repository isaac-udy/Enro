package dev.enro.core

import android.os.Parcel
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlin.io.encoding.Base64

public actual object NKSerializer : KSerializer<NavigationKey> {
    actual override val descriptor: SerialDescriptor = buildClassSerialDescriptor("NavigationKey") {
        element("keyType", String.serializer().descriptor)
        element("serialized", String.serializer().descriptor)
    }

    actual override fun deserialize(decoder: Decoder): NavigationKey {
        lateinit var keyType: String
        lateinit var serialized: String

        decoder.decodeStructure(descriptor) {
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> keyType = decodeStringElement(descriptor, 0)
                    1 -> serialized = decodeStringElement(descriptor, 1)
                    CompositeDecoder.DECODE_DONE -> break // Input is over
                    else -> error("Unexpected index: $index")
                }
            }
        }

        val cls = NavigationKeySerializer.classes[keyType]
            ?: error("Unknown key type: $keyType")

        val parcel = Parcel.obtain()
        val input = Base64.decode(serialized)
        parcel.unmarshall(input, 0, input.size)
        parcel.setDataPosition(0)
        val result = parcel.readBundle(cls.java.classLoader) ?: error("No bundle found in parcel")
        parcel.recycle()

        return NavigationKeySerializer.serializers[keyType]?.deserialize(result)
            ?: error("No NavigationKeySerializer found for key type $keyType")
    }

    actual override fun serialize(encoder: Encoder, value: NavigationKey) {
        val qualifiedName = value::class.qualifiedName
            ?: error("NavigationKeys must have a qualified name - local and anonymous classes are not supported")

        val serializer = NavigationKeySerializer.serializers[qualifiedName] as? NavigationKeySerializer<NavigationKey>
            ?: error("No NavigationKeySerializer found for key type $qualifiedName")

        val savedState = serializer.serialize(value)
        val parcel = Parcel.obtain()
        parcel.writeBundle(savedState)
        val output = parcel.marshall()
        parcel.recycle()

        encoder.encodeStructure(descriptor) {
            @Suppress("UNCHECKED_CAST")
            serializer as NavigationKeySerializer<NavigationKey>

            encodeStringElement(descriptor, 0, qualifiedName)
            encodeStringElement(descriptor, 1, Base64.encode(output))
        }
    }
}
