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
import kotlin.test.assertTrue

@Serializable
@JvmInline
internal value class HistoryTestId(val value: String)

@Serializable
internal data class HistoryTestKey(
    val id: HistoryTestId,
    val name: String = "",
) : NavigationKey

/**
 * Pins the serialization strategy for persisted navigation state (browser
 * history, deep-link tooling) under the controller's json configuration.
 *
 * kotlinx's STREAMING encoder has a bug under
 * `ClassDiscriminatorMode.ALL_JSON_OBJECTS`: it defers each discriminator
 * write until the next `beginStructure`, and a value-class field never opens
 * one — so under polymorphic dispatch (`Instance.key`) the pending
 * discriminator for an inline field (e.g. a typed-id value class on a
 * NavigationKey) leaks into the next object that opens (`Instance.metadata`
 * in practice), producing JSON that fails to decode with "Expected
 * JsonObject, but had JsonLiteral". The TREE encoder (`encodeToJsonElement`)
 * does not share the deferral and produces clean output for the same
 * configuration, so persisted state must be encoded through the tree
 * encoder. [streamingEncoderLeaksDiscriminator] documents the upstream bug:
 * when it starts failing, kotlinx has fixed the streaming encoder and the
 * tree-encode workaround can be retired.
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
    fun treeEncodedInstanceWithValueClassKeyFieldRoundTrips() {
        val json = repository().jsonConfiguration
        val instance = HistoryTestKey(id = HistoryTestId("abc-123"), name = "name").asInstance()

        val encoded = json.encodeToJsonElement(instanceSerializer, instance).toString()
        val decoded = json.decodeFromString(instanceSerializer, encoded)

        assertEquals(instance.id, decoded.id)
        assertEquals(instance.key, decoded.key)
    }

    @Test
    fun treeEncodedInstanceDoesNotLeakDiscriminatorIntoMetadata() {
        val json = repository().jsonConfiguration
        val instance = HistoryTestKey(id = HistoryTestId("abc-123")).asInstance()

        val encoded = json.encodeToJsonElement(instanceSerializer, instance).toString()

        assertFalse(
            encoded.contains("\"metadata\":{\"type\""),
            "discriminator leaked into metadata: $encoded",
        )
        assertFalse(
            encoded.contains("HistoryTestId\""),
            "inline field's class name should not appear anywhere in: $encoded",
        )
    }

    /**
     * Documents the upstream kotlinx streaming-encoder bug that forces the
     * tree-encode strategy. If this test FAILS, kotlinx has fixed the
     * deferred-discriminator leak — the tree-encode workaround in
     * WebHistoryPlugin (and this test) can then be removed.
     */
    @Test
    fun streamingEncoderLeaksDiscriminator() {
        val json = repository().jsonConfiguration
        val instance = HistoryTestKey(id = HistoryTestId("abc-123")).asInstance()

        val encoded = json.encodeToString(instanceSerializer, instance)

        assertTrue(
            encoded.contains("\"metadata\":{\"type\":\"dev.enro.serialization.HistoryTestId\""),
            "kotlinx appears to have fixed the streaming-encoder discriminator leak — " +
                "the tree-encode workaround can be retired. Encoded: $encoded",
        )
    }
}
