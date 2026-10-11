package dev.enro

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MetadataKeyNameJsTest {

    @Test
    fun keysWithTheSameSimpleName_throw() {
        assertEquals("Clash", First.Clash.name)
        assertFailsWith<IllegalStateException> { Second.Clash.name }
    }

    @Test
    fun anExplicitName_avoidsTheClash() {
        assertEquals("Named", First.Named.name)
        assertEquals("second.Named", Second.Named.name)
    }

    @Test
    fun persistedAndTransientKeys_mayShareAName() {
        assertEquals("Shared", First.Shared.name)
        assertEquals("Shared", Second.Shared.name)
    }

    object First {
        object Clash : NavigationKey.MetadataKey<Int>(0)
        object Named : NavigationKey.MetadataKey<Int>(0)
        object Shared : NavigationKey.MetadataKey<Int>(0)
    }

    object Second {
        object Clash : NavigationKey.MetadataKey<Int>(0)
        object Named : NavigationKey.MetadataKey<Int>(0, name = "second.Named")
        object Shared : NavigationKey.TransientMetadataKey<Int>(0)
    }
}
