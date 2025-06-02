package dev.enro3.controller.repository

import androidx.savedstate.serialization.ClassDiscriminatorMode
import androidx.savedstate.serialization.SavedStateConfiguration
import dev.enro3.result.NavigationResultChannel
import dev.enro3.result.flow.FlowStep
import dev.enro3.serialization.WrappedBoolean
import dev.enro3.serialization.WrappedByte
import dev.enro3.serialization.WrappedChar
import dev.enro3.serialization.WrappedDouble
import dev.enro3.serialization.WrappedFloat
import dev.enro3.serialization.WrappedInt
import dev.enro3.serialization.WrappedList
import dev.enro3.serialization.WrappedLong
import dev.enro3.serialization.WrappedMap
import dev.enro3.serialization.WrappedNull
import dev.enro3.serialization.WrappedSet
import dev.enro3.serialization.WrappedShort
import dev.enro3.serialization.WrappedString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.NothingSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

internal class SerializerRepository {
    @OptIn(ExperimentalSerializationApi::class)
    var serializersModule =
        SerializersModule {
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

                subclass(FlowStep.serializer(NothingSerializer()))
                subclass(NavigationResultChannel.Id.serializer())
            }
        }
        private set

    var savedStateConfiguration: SavedStateConfiguration =
        SavedStateConfiguration {
            serializersModule += this@SerializerRepository.serializersModule
            classDiscriminatorMode = ClassDiscriminatorMode.ALL_OBJECTS
        }
        private set

    var jsonConfiguration: Json =
        Json {
            serializersModule += this@SerializerRepository.serializersModule
            classDiscriminatorMode = kotlinx.serialization.json.ClassDiscriminatorMode.ALL_JSON_OBJECTS
            ignoreUnknownKeys = true
            allowStructuredMapKeys = true
        }
        private set

    fun registerSerializersModule(
        serializersModule: SerializersModule,
    ) {
        this.serializersModule += serializersModule
        this.savedStateConfiguration = SavedStateConfiguration(from = savedStateConfiguration) {
            this.serializersModule += serializersModule
        }
        this.jsonConfiguration = Json(from = jsonConfiguration) {
            this.serializersModule += serializersModule
        }
    }
}