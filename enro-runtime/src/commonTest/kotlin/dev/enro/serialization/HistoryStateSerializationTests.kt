package dev.enro.serialization

import dev.enro.NavigationKey
import dev.enro.asInstance
import dev.enro.controller.repository.SerializerRepository
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.jvm.JvmInline
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Serializable
@JvmInline
internal value class HistoryTestId(val value: String)

@Serializable
internal enum class HistoryTestType { NPC, LOCATION }

@Serializable
internal data class HistoryTestKey(
    val id: HistoryTestId,
    val name: String = "",
    val types: Set<HistoryTestType> = emptySet(),
    val excludeIds: Set<HistoryTestId> = emptySet(),
) : NavigationKey

/**
 * Tests the serialization of persisted navigation state (browser history, deep-link tooling)
 * with the controller's Json configuration, which uses the default POLYMORPHIC
 * class-discriminator mode.
 *
 * `ClassDiscriminatorMode.ALL_JSON_OBJECTS` cannot be used. With kotlinx.serialization 1.11,
 * both encoders fail for a polymorphic NavigationKey:
 *
 *  * Streaming: the encoder defers the discriminator of a value-class field until the next
 *    `beginStructure`. An inline field opens no structure, so the encoder writes the
 *    discriminator into the next object (`Instance.metadata`), and decoding fails with
 *    "Expected JsonObject, but had JsonLiteral". A collection field produces invalid JSON
 *    (a `"type"` key-value pair inside an array).
 *  * Tree (`encodeToJsonElement`): a collection field throws
 *    `NumberFormatException: For input string: "type"`. The encoder applies the deferred
 *    discriminator inside a list, where the tag is parsed as an array index.
 *
 * The two `allJsonObjects` tests assert that each failure exists. When one of those tests
 * fails, kotlinx.serialization has fixed the failure and ALL_JSON_OBJECTS can be
 * reconsidered. Upstream issue:
 * https://github.com/Kotlin/kotlinx.serialization/issues/3022
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

    private val fullShapeKey = HistoryTestKey(
        id = HistoryTestId("abc-123"),
        name = "name",
        types = setOf(HistoryTestType.NPC, HistoryTestType.LOCATION),
        excludeIds = setOf(HistoryTestId("excluded")),
    )

    @Test
    fun instanceWithValueClassAndCollectionFieldsRoundTrips() {
        val json = repository().jsonConfiguration
        val instance = fullShapeKey.asInstance()

        val encoded = json.encodeToString(instanceSerializer, instance)
        val decoded = json.decodeFromString(instanceSerializer, encoded)

        assertEquals(instance.id, decoded.id)
        assertEquals(instance.key, decoded.key)
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun allJsonObjectsJson(): Json = Json(from = repository().jsonConfiguration) {
        classDiscriminatorMode = ClassDiscriminatorMode.ALL_JSON_OBJECTS
    }

    // Asserts the upstream streaming-encoder failure. See the class KDoc.
    @Test
    fun allJsonObjectsStreamingEncoderLeaksValueClassDiscriminator() {
        val json = allJsonObjectsJson()
        val instance = HistoryTestKey(id = HistoryTestId("abc-123")).asInstance()

        val encoded = json.encodeToString(instanceSerializer, instance)

        assertTrue(
            encoded.contains("\"metadata\":{\"type\":\"dev.enro.serialization.HistoryTestId\""),
            "kotlinx appears to have fixed the streaming-encoder discriminator leak — " +
                "ALL_JSON_OBJECTS may be viable again. Encoded: $encoded",
        )
    }

    // Asserts the upstream tree-encoder failure. See the class KDoc.
    @Test
    fun allJsonObjectsTreeEncoderFailsOnCollectionFields() {
        val json = allJsonObjectsJson()
        val instance = fullShapeKey.asInstance()

        val result = runCatching {
            json.encodeToJsonElement(instanceSerializer, instance)
        }

        assertTrue(
            result.isFailure,
            "kotlinx appears to have fixed the tree-encoder collection-field crash — " +
                "ALL_JSON_OBJECTS may be viable again. Encoded: ${result.getOrNull()}",
        )
    }
}
