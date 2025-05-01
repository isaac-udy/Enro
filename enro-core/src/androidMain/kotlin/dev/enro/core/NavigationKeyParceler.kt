package dev.enro.core

import android.os.Parcel
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import dev.enro.core.controller.NavigationController
import kotlinx.parcelize.Parceler
import kotlinx.serialization.serializer

public object NavigationKeyParceler : Parceler<NavigationKey> {
    override fun NavigationKey.write(parcel: Parcel, flags: Int) {
        parcel.writeBundle(encodeToSavedState(this, NavigationController.savedStateConfiguration))
    }

    override fun create(parcel: Parcel): NavigationKey {
        return decodeFromSavedState(parcel.readBundle(this::class.java.classLoader)!!, NavigationController.savedStateConfiguration)
    }

    public object Nullable : Parceler<NavigationKey?> {
        override fun NavigationKey?.write(parcel: Parcel, flags: Int) {
            parcel.writeBundle(this?.let { encodeToSavedState(it, NavigationController.savedStateConfiguration) })
        }

        override fun create(parcel: Parcel): NavigationKey? {
            val data = parcel.readBundle(this::class.java.classLoader) ?: return null
            return decodeFromSavedState(data, NavigationController.savedStateConfiguration)
        }
    }
}