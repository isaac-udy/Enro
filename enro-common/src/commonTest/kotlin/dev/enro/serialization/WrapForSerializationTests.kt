package dev.enro.serialization

import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// On Kotlin/JS, Int, Short, Byte and Float are all JS numbers and wrap as WrappedDouble, so these
// pin that each value still reads back equal and with its original type.
class WrapForSerializationTests {

    private val json = Json { serializersModule = serializerModuleForWrapped }

    private fun roundTrip(value: Any): Any {
        val encoded = json.encodeToString(PolymorphicSerializer(Any::class), value.internalWrapForSerialization())
        return json.decodeFromString(PolymorphicSerializer(Any::class), encoded).internalUnwrapForSerialization()
    }

    @Test
    fun numbersRoundTripWithTheirType() {
        val int = roundTrip(42)
        assertEquals(42, int)
        assertTrue(int is Int)

        val long = roundTrip(9_007_199_254_740_993L)
        assertEquals(9_007_199_254_740_993L, long)
        assertTrue(long is Long)

        val short = roundTrip(7.toShort())
        assertEquals(7.toShort(), short)
        assertTrue(short is Short)

        val byte = roundTrip(3.toByte())
        assertEquals(3.toByte(), byte)
        assertTrue(byte is Byte)

        val float = roundTrip(1.5f)
        assertEquals(1.5f, float)
        assertTrue(float is Float)

        val double = roundTrip(2.25)
        assertEquals(2.25, double)
        assertTrue(double is Double)
    }

    @Test
    fun otherPrimitivesRoundTrip() {
        assertEquals(true, roundTrip(true))
        assertEquals("text", roundTrip("text"))
        assertEquals('c', roundTrip('c'))
    }

    @Test
    fun listsOfNumbersRoundTrip() {
        val list = roundTrip(listOf(1, 2, 3))
        assertEquals(listOf(1, 2, 3), list)
        assertTrue((list as List<*>).all { it is Int })
    }
}
