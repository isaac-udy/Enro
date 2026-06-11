package dev.enro.serialization

import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import dev.enro.NavigationKey
import dev.enro.asInstance
import dev.enro.controller.repository.SerializerRepository
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The savedstate analogue of [HistoryStateSerializationTests]: container
 * backstacks (rememberNavigationContainer), flow results, and enroSaver all
 * round-trip through androidx.savedstate serialization with
 * `ClassDiscriminatorMode.ALL_OBJECTS`. This pins that an Instance whose key
 * carries a value-class field survives that round-trip — i.e. that the
 * androidx SavedState encoder does NOT share the kotlinx streaming-JSON
 * encoder's deferred-discriminator leak (see HistoryStateSerializationTests
 * and https://github.com/Kotlin/kotlinx.serialization/issues/3022).
 *
 * Lives in desktopTest rather than commonTest: the serialization logic under
 * test is common, but on the androidHostTest target the Bundle-backed
 * SavedState implementation is not representative of a real device — ANY
 * polymorphic Instance round-trip fails there ("No valid saved state was
 * found for the key 'key'"), including keys with no value classes at all.
 * Desktop's Map-backed SavedState exercises the same common serialization
 * code without the unrepresentative host-Bundle layer; device-faithful
 * Android coverage would need an instrumented test.
 */
class SavedStateSerializationTests {

    private fun repository(): SerializerRepository {
        return SerializerRepository().apply {
            registerSerializersModule(
                SerializersModule {
                    polymorphic(NavigationKey::class) {
                        subclass(HistoryTestKey.serializer())
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
}
