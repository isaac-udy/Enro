package dev.enro.core

import android.os.Parcel
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import kotlinx.parcelize.Parceler

public object NavigationKeyParceler : Parceler<NavigationKey> {
    override fun NavigationKey.write(parcel: Parcel, flags: Int) {
        parcel.writeBundle(encodeToSavedState(NKSerializer, this))
    }

    override fun create(parcel: Parcel): NavigationKey {
        return decodeFromSavedState(NKSerializer, parcel.readBundle(this::class.java.classLoader)!!)
    }

    public object Nullable : Parceler<NavigationKey?> {
        override fun NavigationKey?.write(parcel: Parcel, flags: Int) {
            parcel.writeBundle(this?.let { encodeToSavedState(NKSerializer, it) })
        }

        override fun create(parcel: Parcel): NavigationKey? {
            val data = parcel.readBundle(this::class.java.classLoader) ?: return null
            return decodeFromSavedState(NKSerializer, data)
        }
    }
}