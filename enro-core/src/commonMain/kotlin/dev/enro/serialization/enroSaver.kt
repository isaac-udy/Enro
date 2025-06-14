package dev.enro.serialization

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.saveable.Saver
import androidx.savedstate.SavedState
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer
import androidx.savedstate.read
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import androidx.savedstate.write
import dev.enro.EnroController
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.nullable

/**
 * Creates a [Saver] that uses Enro's savedStateConfiguration to serialize and deserialize objects.
 *
 * This function provides a Saver implementation backed by the savedStateConfiguration registered
 * with Enro, based on classes that are registered in a NavigationModule's serializersModule.
 * This means the serializers are registered with the global Enro Controller.
 *
 * This is particularly useful when you want to remember saved data that:
 * - Belongs to a NavigationKey
 * - Comes from a NavigationKey in some way
 * - Is a NavigationKey or NavigationKey.Instance that you want to remember explicitly
 *
 * @return A [Saver] that can save and restore objects of type [T] using Enro's serialization configuration
 */
public inline fun <reified T : Any> enroSaver(): Saver<T, SavedState> {
    return Saver(
        save = { value ->
            val enroSaverType = EnroSaverType.fromValue(value)
            val saved = encodeToSavedState(
                serializer = enroSaverType.serializer,
                value = value,
                configuration = EnroController.savedStateConfiguration,
            )
            saved.write {
                putString(EnroSaverType.TYPE_KEY, enroSaverType.typeName)
            }
            saved
        },
        restore = { saved ->
            val enroSaverType = EnroSaverType.forSavedState<T>(saved)
            decodeFromSavedState(
                deserializer = enroSaverType.serializer,
                savedState = saved,
                configuration = EnroController.savedStateConfiguration,
            )
        }
    )
}

@PublishedApi
internal class EnroSaverType<T>(
    val typeName: String,
    val serializer: KSerializer<T>
) {
    companion object {
        const val TYPE_KEY = "\$\$enroSaverType"

        fun <T: Any> fromValue(
            value: T,
        ): EnroSaverType<T> {
            lateinit var typeName: String

            val serializer = when(value) {
                is MutableState<*> -> {
                    typeName = "MutableState"
                    MutableStateSerializer(PolymorphicSerializer(Any::class).nullable)
                }
                else -> {
                    typeName = "Any"
                    PolymorphicSerializer(Any::class)
                }
            }

            @Suppress("UNCHECKED_CAST")
            return EnroSaverType(
                typeName = typeName,
                serializer = serializer as KSerializer<T>
            )
        }

        fun <T: Any> forSavedState(savedState: SavedState): EnroSaverType<T> {
            val typeName = savedState.read {
                getString(TYPE_KEY)
            }
            requireNotNull(typeName) {
                "SavedState does not contain an EnroSaverType"
            }
            @Suppress("UNCHECKED_CAST")
            return when(typeName) {
                "MutableState" -> EnroSaverType(
                    typeName = typeName,
                    serializer = MutableStateSerializer(PolymorphicSerializer(Any::class).nullable)
                )
                else -> EnroSaverType(
                    typeName = typeName,
                    serializer = PolymorphicSerializer(Any::class)
                )
            } as EnroSaverType<T>
        }
    }
}