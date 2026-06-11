package dev.enro.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.jvm.JvmInline
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@Serializable
@JvmInline
internal value class TestValueId(val value: String)

class BoxedValueClassSerializerTests {

    @OptIn(ExperimentalSerializationApi::class)
    private fun json(builder: kotlinx.serialization.modules.PolymorphicModuleBuilder<Any>.() -> Unit): Json {
        return Json {
            serializersModule = SerializersModule {
                polymorphic(Any::class) { builder() }
            }
            classDiscriminatorMode = ClassDiscriminatorMode.ALL_JSON_OBJECTS
            ignoreUnknownKeys = true
        }
    }

    private val polymorphicAny = PolymorphicSerializer(Any::class)
    private val anyMapSerializer = MapSerializer(String.serializer(), polymorphicAny)

    @Test
    fun boxedValueClassRoundTripsPolymorphically() {
        val json = json { valueClassSubclass(TestValueId.serializer()) }
        val original: Any = TestValueId("abc-123")

        val encoded = json.encodeToString(polymorphicAny, original)
        assertTrue(
            encoded.contains("\"value\":\"abc-123\""),
            "expected boxed envelope, was: $encoded",
        )

        val decoded = json.decodeFromString(polymorphicAny, encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun boxedValueClassRoundTripsInsidePolymorphicMap() {
        val json = json { valueClassSubclass(TestValueId.serializer()) }
        val original: Map<String, Any> = mapOf("id" to TestValueId("abc-123"))

        val encoded = json.encodeToString(anyMapSerializer, original)
        val decoded = json.decodeFromString(anyMapSerializer, encoded)

        assertEquals(original, decoded)
    }

    /**
     * Documents the kotlinx behaviour this serializer exists to work around:
     * a plain `subclass(...)` registration encodes a polymorphic value class
     * as its bare literal, which then fails to decode. If this test starts
     * failing because the decode succeeds, kotlinx has fixed the asymmetry
     * upstream and [BoxedValueClassSerializer] may no longer be necessary.
     */
    @Test
    fun plainSubclassRegistrationDoesNotRoundTrip() {
        val json = json { subclass(TestValueId.serializer()) }
        val original: Any = TestValueId("abc-123")

        val encoded = json.encodeToString(polymorphicAny, original)
        assertEquals("\"abc-123\"", encoded, "bare literal, no discriminator")

        assertFailsWith<kotlinx.serialization.SerializationException> {
            json.decodeFromString(polymorphicAny, encoded)
        }
    }
}
