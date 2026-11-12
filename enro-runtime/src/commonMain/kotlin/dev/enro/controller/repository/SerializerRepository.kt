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
            // The discriminator key of savedstate is always "type". ALL_OBJECTS writes "type" into
            // every class and rejects a serialized property named "type".
            classDiscriminatorMode = ClassDiscriminatorMode.POLYMORPHIC
        }
        private set

    @OptIn(ExperimentalSerializationApi::class)
    var jsonConfiguration: Json =
        Json {
            serializersModule = this@SerializerRepository.serializersModule
            // The default POLYMORPHIC discriminator mode is deliberate. With kotlinx.serialization
            // 1.11, ALL_JSON_OBJECTS writes a misplaced discriminator after a value-class field
            // and fails on a collection field of a polymorphic NavigationKey:
            // https://github.com/Kotlin/kotlinx.serialization/issues/3022
            // HistoryStateSerializationTests asserts each ALL_JSON_OBJECTS failure, so a
            // kotlinx.serialization upgrade that fixes the failures fails those tests.
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