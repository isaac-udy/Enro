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
 * Pins the serialization of persisted navigation state (browser history,
 * deep-link tooling) under the controller's json configuration, which uses
 * the default (POLYMORPHIC) class-discriminator mode.
 *
 * `ClassDiscriminatorMode.ALL_JSON_OBJECTS` cannot be used: with kotlinx
 * 1.11, realistic NavigationKey shapes break BOTH encoders under polymorphic
 * dispatch —
 *
 *  * STREAMING: a value-class field's discriminator write is deferred until
 *    the next `beginStructure`, which an inline field never opens, so the
 *    pending discriminator leaks into the next-opened object
 *    (`Instance.metadata` in practice) and the output fails to decode with
 *    "Expected JsonObject, but had JsonLiteral". Collection fields produce
 *    outright INVALID JSON (a `"type"` key:value pair inside an array).
 *  * TREE (`encodeToJsonElement`): collection fields crash the encoder with
 *    `NumberFormatException: For input string: "type"` (the deferred
 *    discriminator is applied inside a list context, where the tag is parsed
 *    as an array index).
 *
 * The documenting tests below assert those failures still exist: when one
 * starts failing, kotlinx has fixed the corresponding bug and the
 * POLYMORPHIC-mode constraint can be revisited. Upstream issue:
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

    /**
     * Documents the upstream kotlinx streaming-encoder bug. If this test
     * FAILS, kotlinx has fixed the deferred-discriminator leak for inline
     * fields and ALL_JSON_OBJECTS may be viable again (check the other
     * documenting tests too).
     */
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

    /**
     * Documents the upstream kotlinx tree-encoder bug. If this test FAILS,
     * kotlinx has fixed the collection-field discriminator crash and
     * ALL_JSON_OBJECTS may be viable again (check the other documenting
     * tests too).
     */
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
