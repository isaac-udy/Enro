package dev.enro.core

import android.os.Parcel
import android.os.Parcelable
import androidx.core.os.ParcelCompat
import kotlin.io.encoding.Base64
import kotlin.reflect.KClass

public inline fun <reified T> NavigationKeySerializer.Companion.forParcelable(): NavigationKeySerializer<T>
        where T : NavigationKey, T : Parcelable {
    return forParcelable(T::class)
}

@PublishedApi
internal fun <T> NavigationKeySerializer.Companion.forParcelable(cls: KClass<T>): NavigationKeySerializer<T>
        where T : NavigationKey, T : Parcelable {
    return NavigationKeySerializerForParcelable(cls)
}

@PublishedApi
internal class NavigationKeySerializerForParcelable<T>(
    private val cls: KClass<T>,
) : NavigationKeySerializer<T>(cls) where T : NavigationKey {
    override fun serialize(key: T): String {
        val parcel = Parcel.obtain()
        key as Parcelable

        parcel.writeParcelable(key, 0)
        val output = parcel.marshall()
        parcel.recycle()
        return Base64.encode(output)
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(data: String): T {
        val parcel = Parcel.obtain()
        val input = Base64.decode(data)
        parcel.unmarshall(input, 0, input.size)
        parcel.setDataPosition(0)
        val result = ParcelCompat.readParcelable(parcel, cls.java.classLoader, cls.java as Class<out Parcelable>)
        parcel.recycle()
        return result as? T
            ?: error("Failed to deserialize Parcelable NavigationKey of type $cls")
    }
}
