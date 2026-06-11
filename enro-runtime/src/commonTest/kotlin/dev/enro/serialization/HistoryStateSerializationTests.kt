package dev.enro.serialization

import dev.enro.NavigationKey
import dev.enro.asInstance
import dev.enro.controller.repository.SerializerRepository
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.jvm.JvmInline
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@Serializable
@JvmInline
internal value class HistoryTestId(val value: String)

@Serializable
internal data class HistoryTestKey(
    val id: HistoryTestId,
    val name: String = "",
) : NavigationKey

/**
 * Regression tests for the serialized form of [NavigationKey.Instance] under
 * the controller's json configuration.
 *
 * Under `ClassDiscriminatorMode.ALL_JSON_OBJECTS`, kotlinx defers each
 * discriminator write until the next `beginStructure` — and a value-class
 * field never opens a structure, so the pending discriminator for an inline
 * field (e.g. a typed-id value class on a NavigationKey) leaked into the next
 * object that opened: in practice `Instance.metadata`, which then failed to
 * decode ("Expected JsonObject, but had JsonLiteral"). These tests pin the
 * fixed (default, POLYMORPHIC) discriminator mode: instances whose keys carry
 * value-class fields must round-trip, with no discriminator leakage.
 */
class HistoryStateSerializationTests {

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
    fun instanceWithValueClassKeyFieldRoundTrips() {
        val json = repository().jsonConfiguration
        val instance = HistoryTestKey(id = HistoryTestId("abc-123"), name = "name").asInstance()

        val encoded = json.encodeToString(instanceSerializer, instance)
        val decoded = json.decodeFromString(instanceSerializer, encoded)

        assertEquals(instance.id, decoded.id)
        assertEquals(instance.key, decoded.key)
    }

    @Test
    fun valueClassFieldDiscriminatorDoesNotLeakIntoMetadata() {
        val json = repository().jsonConfiguration
        val instance = HistoryTestKey(id = HistoryTestId("abc-123")).asInstance()

        val encoded = json.encodeToString(instanceSerializer, instance)

        // The leak's signature: the inline field's pending discriminator lands
        // inside the next-opened object, so metadata gains a bogus
        // {"type": "...HistoryTestId"} entry.
        assertFalse(
            encoded.contains("\"metadata\":{\"type\""),
            "discriminator leaked into metadata: $encoded",
        )
        assertFalse(
            encoded.contains("HistoryTestId\""),
            "inline field's class name should not appear anywhere in: $encoded",
        )
    }
}
