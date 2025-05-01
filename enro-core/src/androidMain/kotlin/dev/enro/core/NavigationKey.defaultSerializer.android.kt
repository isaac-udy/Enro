package dev.enro.core

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Base64
import android.util.Log
import androidx.core.os.BundleCompat
import androidx.core.os.ParcelCompat
import androidx.savedstate.savedState
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import androidx.savedstate.serialization.serializers.SavedStateSerializer
import dev.enro.core.controller.NavigationController
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
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

public actual inline fun <reified T : NavigationKey> NavigationKey.Companion.defaultSerializer(): KSerializer<T> {
    val serializer = runCatching { serializer<T>() }
        .getOrNull()
    if (serializer != null) {
        return serializer
    }
    return ParcelableNavigationKeySerializer(T::class)
}

public class ParcelableNavigationKeySerializer<T : NavigationKey>(
    private val type: KClass<T>,
) : KSerializer<T> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("${type.qualifiedName}") {
        element("navigationKey", String.serializer().descriptor)
    }

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
                return@decodeStructure BundleCompat.getParcelable(readState, "navigationKey", type.java) as T
            }
        }
        val state = SavedStateSerializer.deserialize(decoder) as Bundle
        return BundleCompat.getParcelable(state, "navigationKey", type.java) as T
    }

    override fun serialize(encoder: Encoder, value: T) {
        if (encoder is JsonEncoder) {
            value as Parcelable
            val data = Bundle().apply {
                putParcelable("navigationKey", value)
            }
            val parcel = Parcel.obtain()
            data.writeToParcel(parcel, 0)
            val base64Encoded = Base64.encodeToString(parcel.marshall(), Base64.DEFAULT)
            parcel.recycle()

            Log.e("Parcelable", "encoded: $base64Encoded")
            encoder.encodeStructure(descriptor) {
                encodeStringElement(
                    descriptor,
                    0,
                    base64Encoded,
                )
            }
            return
        }

        SavedStateSerializer.serialize(encoder, savedState {
            putParcelable("navigationKey", value as Parcelable)
        })
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
        ): ParcelableNavigationKeySerializer<T> {
            return ParcelableNavigationKeySerializer(type.kotlin)
        }
    }
}

/**
 * This is a java.lang.Class based factory method to create a KSerializer for a particular
 * NavigationKey type. This is primarily used by the KAPT annotation processor when
 * generating Java code, and has some limitations
 */
public object KSerializerForJava {
    @JvmStatic
    public fun <T : NavigationKey> create(type: Class<T>): KSerializer<T> {
        return serializer(
            kClass = type.kotlin,
            typeArgumentsSerializers = emptyList(),
            isNullable = false
        ) as KSerializer<T>
    }
}
