package dev.enro.serialization

import androidx.savedstate.serialization.ClassDiscriminatorMode
import androidx.savedstate.serialization.SavedStateConfiguration
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import dev.enro.NavigationKey
import dev.enro.asInstance
import dev.enro.controller.repository.SerializerRepository
import dev.enro.test.platform.RobolectricHostTest
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The savedstate analogue of [HistoryStateSerializationTests]: container
 * backstacks (rememberNavigationContainer), flow results, and enroSaver all
 * round-trip through androidx.savedstate serialization with
 * `ClassDiscriminatorMode.POLYMORPHIC`. This pins that an Instance whose key
 * carries a value-class field survives that round-trip — i.e. that the
 * androidx SavedState encoder does NOT share the kotlinx streaming-JSON
 * encoder's deferred-discriminator leak (see HistoryStateSerializationTests).
 */
class SavedStateSerializationTests : RobolectricHostTest() {

    private fun repository(): SerializerRepository {
        return SerializerRepository().apply {
            registerSerializersModule(
                SerializersModule {
                    polymorphic(NavigationKey::class) {
                        subclass(HistoryTestKey.serializer())
                        subclass(TypePropertyTestKey.serializer())
                    }
                }
            )
        }
    }

    private val instanceSerializer =
        NavigationKey.Instance.serializer(PolymorphicSerializer(NavigationKey::class))

    @Test
    fun instanceWithValueClassKeyFieldRoundTripsThroughSavedState() {
        val configuration = repository().savedStateConfiguration
        val instance = HistoryTestKey(id = HistoryTestId("abc-123"), name = "name").asInstance()

        val saved = encodeToSavedState(instanceSerializer, instance, configuration)
        val decoded = decodeFromSavedState(instanceSerializer, saved, configuration)

        assertEquals(instance.id, decoded.id)
        assertEquals(instance.key, decoded.key)
    }

    @Test
    fun instanceWithPropertyNamedTypeRoundTripsThroughSavedState() {
        val configuration = repository().savedStateConfiguration
        val instance = TypePropertyTestKey(type = "admin", nested = TypePropertyTestValue(type = 3)).asInstance()

        val saved = encodeToSavedState(instanceSerializer, instance, configuration)
        val decoded = decodeFromSavedState(instanceSerializer, saved, configuration)

        assertEquals(instance.id, decoded.id)
        assertEquals(instance.key, decoded.key)
    }

    @Test
    fun stateSavedWithAllObjectsDiscriminatorsStillDecodes() {
        val configuration = repository().savedStateConfiguration
        val allObjectsConfiguration = SavedStateConfiguration(from = configuration) {
            classDiscriminatorMode = ClassDiscriminatorMode.ALL_OBJECTS
        }
        val instance = HistoryTestKey(id = HistoryTestId("abc-123"), name = "name").asInstance()

        val saved = encodeToSavedState(instanceSerializer, instance, allObjectsConfiguration)
        val decoded = decodeFromSavedState(instanceSerializer, saved, configuration)

        assertEquals(instance.id, decoded.id)
        assertEquals(instance.key, decoded.key)
    }
}

@Serializable
internal data class TypePropertyTestValue(val type: Int)

@Serializable
internal data class TypePropertyTestKey(
    val type: String,
    val nested: TypePropertyTestValue,
) : NavigationKey
