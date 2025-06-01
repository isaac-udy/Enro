package dev.enro3.controller.repository

import androidx.savedstate.serialization.ClassDiscriminatorMode
import androidx.savedstate.serialization.SavedStateConfiguration
import dev.enro3.serialization.*
import dev.enro3.result.NavigationResultChannel
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

internal class SerializerRepository {
    var serializersModule = SerializersModule {
        polymorphic(Any::class) {
            subclass(WrappedBoolean.serializer())
            subclass(WrappedDouble.serializer())
            subclass(WrappedFloat.serializer())
            subclass(WrappedInt.serializer())
            subclass(WrappedLong.serializer())
            subclass(WrappedShort.serializer())
            subclass(WrappedString.serializer())
            subclass(WrappedByte.serializer())
            subclass(WrappedChar.serializer())
            subclass(WrappedNull.serializer())

            subclass(WrappedList.serializer())
            subclass(WrappedSet.serializer())
            subclass(WrappedMap.serializer())

            subclass(NavigationResultChannel.Id.serializer())
        }
    }
        private set

    private var savedStateConfiguration: SavedStateConfiguration = SavedStateConfiguration {
        serializersModule += this@SerializerRepository.serializersModule
        classDiscriminatorMode = ClassDiscriminatorMode.ALL_OBJECTS
    }

    private var jsonConfiguration: Json = Json {
        serializersModule += this@SerializerRepository.serializersModule
        classDiscriminatorMode = kotlinx.serialization.json.ClassDiscriminatorMode.ALL_JSON_OBJECTS
        ignoreUnknownKeys = true
        allowStructuredMapKeys = true
    }

    fun registerSerializersModule(
        serializersModule: SerializersModule
    ) {
        this.serializersModule += serializersModule
        this.savedStateConfiguration = SavedStateConfiguration(from = savedStateConfiguration) {
            this.serializersModule += serializersModule
        }
        this.jsonConfiguration = Json(from = jsonConfiguration) {
            this.serializersModule += serializersModule
        }
    }

    fun getSavedStateConfiguration(): SavedStateConfiguration {
        return savedStateConfiguration
    }

    fun getJsonConfiguration(): Json {
        return jsonConfiguration
    }
}