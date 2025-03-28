package dev.enro.core

import android.os.Parcel
import kotlinx.parcelize.Parceler

public object NavigationKeyParceler : Parceler<NavigationKey> {
    override fun NavigationKey.write(parcel: Parcel, flags: Int) {
        parcel.writeString(NavigationKeySerializer.serialize(this))
    }

    override fun create(parcel: Parcel): NavigationKey {
        val data = parcel.readString() ?: error("Failed to read NavigationKey from Parcel - readString returned null")
        return NavigationKeySerializer.deserialize(data)
    }

    public object Nullable : Parceler<NavigationKey?> {
        override fun NavigationKey?.write(parcel: Parcel, flags: Int) {
            parcel.writeString(this?.let { NavigationKeySerializer.serialize(it) })
        }

        override fun create(parcel: Parcel): NavigationKey? {
            val data = parcel.readString()
            return data?.let { NavigationKeySerializer.deserialize(it) }
        }
    }
}