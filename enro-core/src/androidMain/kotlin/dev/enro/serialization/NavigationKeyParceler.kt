package dev.enro.serialization

import android.os.Parcel
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import dev.enro.EnroController
import dev.enro.NavigationKey
import kotlinx.android.parcel.Parceler
import kotlinx.serialization.PolymorphicSerializer

public object NavigationKeyParceler : Parceler<NavigationKey> {
    override fun NavigationKey.write(parcel: Parcel, flags: Int) {
        parcel.writeBundle(
            encodeToSavedState(
                serializer = PolymorphicSerializer(NavigationKey::class),
                value = this,
                configuration = EnroController.savedStateConfiguration
            )
        )
    }

    override fun create(parcel: Parcel): NavigationKey {
        return decodeFromSavedState(
            deserializer = PolymorphicSerializer(NavigationKey::class),
            savedState = parcel.readBundle(this::class.java.classLoader)!!,
            configuration = EnroController.savedStateConfiguration
        )
    }

    public object Nullable : Parceler<NavigationKey?> {
        override fun NavigationKey?.write(parcel: Parcel, flags: Int) {
            parcel.writeBundle(this?.let {
                encodeToSavedState(
                    serializer = PolymorphicSerializer(NavigationKey::class),
                    value = it,
                    configuration = EnroController.savedStateConfiguration
                )
            })
        }

        override fun create(parcel: Parcel): NavigationKey? {
            val data = parcel.readBundle(this::class.java.classLoader) ?: return null
            return decodeFromSavedState(
                deserializer = PolymorphicSerializer(NavigationKey::class),
                savedState = data,
                configuration = EnroController.savedStateConfiguration
            )
        }
    }
}