package dev.enro.core.serialization

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Base64
import androidx.core.os.BundleCompat
import androidx.savedstate.serialization.serializers.ParcelableSerializer
import dev.enro.core.NavigationKey
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
import kotlin.reflect.KClass

public class SerializerForParcelableNavigationKey<T : NavigationKey>(
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
                val base64Decoded = Base64.decode(base64Encoded, Base64.DEFAULT)
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
            val base64Encoded = Base64.encodeToString(parcel.marshall(), Base64.DEFAULT)
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

    public companion object {
        /**
         * This is a java.lang.Class based factory method to create a ParcelableNavigationKeySerializer,
         * which is primarily used by the KAPT annotation processor when generating code for Parcelable
         * NavigationKeys.
         */
        @JvmStatic
        public fun <T: NavigationKey> create(
            type: Class<T>,
        ): SerializerForParcelableNavigationKey<T> {
            return SerializerForParcelableNavigationKey(type.kotlin)
        }
    }
}