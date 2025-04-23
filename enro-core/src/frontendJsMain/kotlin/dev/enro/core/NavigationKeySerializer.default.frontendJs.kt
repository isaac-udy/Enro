@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
package dev.enro.core

import androidx.savedstate.SavedState
import androidx.savedstate.savedState
import kotlinx.serialization.InternalSerializationApi
import kotlin.reflect.KClass

@OptIn(InternalSerializationApi::class)
public actual fun <T : NavigationKey> NavigationKeySerializer.Companion.default(
    cls: KClass<T>,
): NavigationKeySerializer<T> {
    // TODO this needs to be fixed; we should not need to rely on "INVISIBLE_REFERENCE"/"INVISIBLE_MEMBER" and the internal map
    return object : NavigationKeySerializer<T>(cls) {
        private val SAVED_NAME = "dev.enro.core.NavigationKeySerializer.SAVED"
        override fun serialize(key: T): SavedState {
            return savedState().apply {
                map.put(SAVED_NAME, key)
            }
        }

        override fun deserialize(data: SavedState): T {
            return data.map[SAVED_NAME] as T
        }
    }
}