package dev.enro.core.controller.repository

import androidx.savedstate.serialization.SavedStateConfiguration
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.internal.isDebugBuild
import dev.enro.core.result.flows.FlowStep
import dev.enro.core.serialization.WrappedBoolean
import dev.enro.core.serialization.WrappedByte
import dev.enro.core.serialization.WrappedChar
import dev.enro.core.serialization.WrappedDouble
import dev.enro.core.serialization.WrappedFloat
import dev.enro.core.serialization.WrappedInt
import dev.enro.core.serialization.WrappedList
import dev.enro.core.serialization.WrappedLong
import dev.enro.core.serialization.WrappedMap
import dev.enro.core.serialization.WrappedNull
import dev.enro.core.serialization.WrappedSet
import dev.enro.core.serialization.WrappedShort
import dev.enro.core.serialization.WrappedString
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.NothingSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

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