package dev.enro.core

import androidx.savedstate.SavedState
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

@OptIn(InternalSerializationApi::class)
public actual fun <T : NavigationKey> NavigationKeySerializer.Companion.default(
    cls: KClass<T>,
): NavigationKeySerializer<T> {
    return object : NavigationKeySerializer<T>(cls) {
        override fun serialize(key: T): SavedState {
            return encodeToSavedState(cls.serializer(), key)
        }

        override fun deserialize(data: SavedState): T {
            return decodeFromSavedState(cls.serializer(), data)
        }
    }
}