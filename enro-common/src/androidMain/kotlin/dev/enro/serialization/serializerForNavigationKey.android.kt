package dev.enro.serialization

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.core.os.BundleCompat
import androidx.savedstate.serialization.serializers.ParcelableSerializer
import dev.enro.NavigationKey
import dev.enro.annotations.AdvancedEnroApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlin.io.encoding.Base64
import kotlin.reflect.KClass

@AdvancedEnroApi
public actual inline fun <reified T : NavigationKey> serializerForNavigationKey(): KSerializer<T> {
    val serializer = runCatching { defaultSerializerForNavigationKey<T>() }
        .getOrNull()
    if (serializer != null) {
        return serializer
    }
    return SerializerForParcelableNavigationKey(T::class)
}

@PublishedApi
internal class SerializerForParcelableNavigationKey<T : NavigationKey>(
    private val type: KClass<T>,
) : KSerializer<T> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("${type.qualifiedName}") {
            element("value", String.serializer().descriptor)
        }
    private val parcelableSerializer = object : ParcelableSerializer<Parcelable>() {}

    override fun deserialize(decoder: Decoder): T {
        if (decoder is JsonDecoder) {
            return decoder.decodeStructure(descriptor) {
                val base64Encoded = decodeStringElement(
                    descriptor = descriptor,
                    index = decodeElementIndex(descriptor)
                )
                val base64Decoded = Base64.decode(base64Encoded)
                val savedParcel = Parcel.obtain().apply {
                    unmarshall(base64Decoded, 0, base64Decoded.size)
                }
                savedParcel.setDataPosition(0)
                val readState = savedParcel.readBundle(type.java.classLoader)!!
                savedParcel.recycle()
                return@decodeStructure BundleCompat.getParcelable(readState, "value", type.java) as T
            }
        }
        return parcelableSerializer.deserialize(decoder) as T
    }

    override fun serialize(encoder: Encoder, value: T) {
        if (encoder is JsonEncoder) {
            value as Parcelable
            val data = Bundle().apply {
                putParcelable("value", value)
            }
            val parcel = Parcel.obtain()
            data.writeToParcel(parcel, 0)
            val base64Encoded = Base64.encode(parcel.marshall())
            parcel.recycle()
            encoder.encodeStructure(descriptor) {
                encodeStringElement(
                    descriptor,
                    0,
                    base64Encoded,
                )
            }
            return
        }

        parcelableSerializer.serialize(encoder, value as Parcelable)
    }
}