package dev.enro.core

import android.os.Parcelable
import androidx.savedstate.SavedState
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import androidx.savedstate.serialization.serializers.ParcelableSerializer
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

@OptIn(InternalSerializationApi::class)
public actual fun <T : NavigationKey> NavigationKeySerializer.Companion.default(
    cls: KClass<T>,
): NavigationKeySerializer<T> {
    if (cls.isSubclassOf(Parcelable::class)) {
        val serializer = object : ParcelableSerializer<Parcelable>() {}
        return object : NavigationKeySerializer<T>(cls) {
            override fun serialize(key: T): SavedState {
                return encodeToSavedState(serializer, key as Parcelable)
            }

            override fun deserialize(data: SavedState): T {
                return decodeFromSavedState(serializer, data) as T
            }
        }
    }
    return object : NavigationKeySerializer<T>(cls) {
        override fun serialize(key: T): SavedState {
            return encodeToSavedState(cls.serializer(), key)
        }

        override fun deserialize(data: SavedState): T {
            return decodeFromSavedState(cls.serializer(), data)
        }
    }
}
