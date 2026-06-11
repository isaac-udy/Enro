package dev.enro.controller.repository

import androidx.savedstate.serialization.ClassDiscriminatorMode
import androidx.savedstate.serialization.SavedStateConfiguration
import dev.enro.NavigationKey
import dev.enro.result.NavigationResultChannel
import dev.enro.result.flow.FlowStep
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.NothingSerializer
import kotlinx.serialization.builtins.serializer
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
            @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
            include(dev.enro.serialization.serializerModuleForWrapped)

            polymorphic(Any::class) {
                subclass(Unit.serializer())
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
            // Deliberately the default (POLYMORPHIC) discriminator mode, NOT
            // ALL_JSON_OBJECTS: kotlinx defers the discriminator write until
            // the next beginStructure, and a value-class field never opens
            // one — so under ALL_JSON_OBJECTS the pending discriminator for an
            // inline field (e.g. a typed-id value class on a NavigationKey)
            // leaks into whatever object opens next (Instance.metadata, in
            // practice), corrupting the serialized form so it can't decode.
            // POLYMORPHIC mode writes discriminators exactly where polymorphic
            // deserialization needs them (Instance.key, metadata values) and
            // nowhere else. Covered by HistoryStateSerializationTests.
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