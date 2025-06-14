package dev.enro.controller.repository

import androidx.savedstate.serialization.ClassDiscriminatorMode
import androidx.savedstate.serialization.SavedStateConfiguration
import dev.enro.NavigationKey
import dev.enro.result.NavigationResultChannel
import dev.enro.result.flow.FlowStep
import dev.enro.serialization.WrappedBoolean
import dev.enro.serialization.WrappedByte
import dev.enro.serialization.WrappedChar
import dev.enro.serialization.WrappedDouble
import dev.enro.serialization.WrappedFloat
import dev.enro.serialization.WrappedInt
import dev.enro.serialization.WrappedList
import dev.enro.serialization.WrappedLong
import dev.enro.serialization.WrappedMap
import dev.enro.serialization.WrappedNull
import dev.enro.serialization.WrappedSet
import dev.enro.serialization.WrappedShort
import dev.enro.serialization.WrappedString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.NothingSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
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
            contextual<NavigationKey.Instance<NavigationKey>>(
                NavigationKey.Instance.serializer(PolymorphicSerializer(NavigationKey::class))
            )
        }
        private set

    var savedStateConfiguration: SavedStateConfiguration =
        SavedStateConfiguration {
            serializersModule = this@SerializerRepository.serializersModule
            classDiscriminatorMode = ClassDiscriminatorMode.ALL_OBJECTS
        }
        private set

    @OptIn(ExperimentalSerializationApi::class)
    var jsonConfiguration: Json =
        Json {
            serializersModule = this@SerializerRepository.serializersModule
            classDiscriminatorMode = kotlinx.serialization.json.ClassDiscriminatorMode.ALL_JSON_OBJECTS
            ignoreUnknownKeys = true
        }
        private set

    fun registerSerializersModule(
        serializersModule: SerializersModule,
    ) {
        this.serializersModule += serializersModule
        this.savedStateConfiguration = SavedStateConfiguration(from = savedStateConfiguration) {
            this@SavedStateConfiguration.serializersModule = this@SerializerRepository.serializersModule
        }
        this.jsonConfiguration = Json(from = jsonConfiguration) {
            this@Json.serializersModule = this@SerializerRepository.serializersModule
        }
    }
}