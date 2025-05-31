package dev.enro.core.controller.repository

import androidx.savedstate.serialization.SavedStateConfiguration
import dev.enro3.result.NavigationResultChannel
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.internal.isDebugBuild
import dev.enro.core.result.flows.FlowStep
import dev.enro.core.result.internal.ResultChannelId
import dev.enro.core.serialization.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.NothingSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.*

internal class SerializerRepository {
    var serializersModule = SerializersModule {
        contextual(NavigationInstruction.Open.serializer(NavigationDirection.serializer()))
        contextual(NavigationDirection.serializer())
        contextual(NavigationDirection.Push.serializer())
        contextual(NavigationDirection.Present.serializer())

        polymorphic(Any::class) {
            subclass(NavigationInstruction.Open.serializer(NavigationDirection.serializer()))
            subclass(NavigationDirection.serializer())
            subclass(NavigationDirection.Push.serializer())
            subclass(NavigationDirection.Present.serializer())
            subclass(ResultChannelId.serializer())

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

        polymorphic(NavigationKey::class) {
            subclass(FlowStep::class, FlowStep.serializer(NothingSerializer()) as KSerializer<FlowStep<*>>)
        }
    }
        private set

    private var savedStateConfiguration: SavedStateConfiguration = SavedStateConfiguration {
        serializersModule += this@SerializerRepository.serializersModule
        classDiscriminatorMode = androidx.savedstate.serialization.ClassDiscriminatorMode.ALL_OBJECTS
    }

    private var jsonConfiguration: Json = Json {
        serializersModule += this@SerializerRepository.serializersModule
        classDiscriminatorMode = kotlinx.serialization.json.ClassDiscriminatorMode.ALL_JSON_OBJECTS
        ignoreUnknownKeys = true
        allowStructuredMapKeys = true
        prettyPrint = isDebugBuild()
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