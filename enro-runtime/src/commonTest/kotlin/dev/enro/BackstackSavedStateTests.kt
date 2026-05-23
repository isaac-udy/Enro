package dev.enro

import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import dev.enro.test.EnroTest
import dev.enro.test.NavigationKeyFixtures
import dev.enro.test.runEnroTest
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests that lock down the saved-state round-trip path used by
 * [dev.enro.ui.rememberNavigationContainer]'s Saver and by
 * [dev.enro.ui.NavigationContainerState.saveState]. Both call sites
 * encode each [NavigationKey.Instance] in the backstack via
 * `NavigationKey.Instance.serializer(PolymorphicSerializer(NavigationKey::class))`
 * and the controller's `savedStateConfiguration`. If that path breaks,
 * backstacks won't survive process death — these tests are the cheapest
 * regression catcher for that class of bug.
 *
 * commonTest doesn't go through navigation binding registration, so the
 * test helper explicitly registers [NavigationKeyFixtures.SimpleKey] with
 * the controller's polymorphic `NavigationKey` resolver.
 */
class BackstackSavedStateTests {

    private object PersistentTestMetadataKey :
        NavigationKey.MetadataKey<String>(default = "default-persistent")

    private object TransientTestMetadataKey :
        NavigationKey.TransientMetadataKey<String>(default = "default-transient")

    private fun runWithSimpleKeySerializer(block: () -> Unit) = runEnroTest {
        EnroTest.getCurrentNavigationController()
            .serializers
            .registerSerializersModule(
                SerializersModule {
                    polymorphic(NavigationKey::class) {
                        subclass(
                            NavigationKeyFixtures.SimpleKey::class,
                            NavigationKeyFixtures.SimpleKey.serializer(),
                        )
                    }
                }
            )
        block()
    }

    @Test
    fun `NavigationKey Instance round-trips through encodeToSavedState`() = runWithSimpleKeySerializer {
        val configuration = EnroController.savedStateConfiguration
        val serializer = NavigationKey.Instance.serializer(PolymorphicSerializer(NavigationKey::class))

        val original = NavigationKeyFixtures.SimpleKey().asInstance()

        val encoded = encodeToSavedState(serializer, original, configuration)
        val decoded = decodeFromSavedState(serializer, encoded, configuration)

        assertEquals(original.id, decoded.id, "Instance id should round-trip unchanged")
        assertEquals(original.key, decoded.key, "Instance key should round-trip unchanged")
    }

    @Test
    fun `Backstack of multiple instances round-trips through encodeToSavedState`() = runWithSimpleKeySerializer {
        val configuration = EnroController.savedStateConfiguration
        val serializer = NavigationKey.Instance.serializer(PolymorphicSerializer(NavigationKey::class))

        val originals = listOf(
            NavigationKeyFixtures.SimpleKey().asInstance(),
            NavigationKeyFixtures.SimpleKey().asInstance(),
            NavigationKeyFixtures.SimpleKey().asInstance(),
        )

        val roundTripped = originals.map { instance ->
            decodeFromSavedState(serializer, encodeToSavedState(serializer, instance, configuration), configuration)
        }

        assertEquals(originals.size, roundTripped.size)
        originals.zip(roundTripped).forEachIndexed { index, (original, decoded) ->
            assertEquals(original.id, decoded.id, "Instance at index $index lost its id")
            assertEquals(original.key, decoded.key, "Instance at index $index lost its key")
        }
    }

    @Test
    fun `Persistent metadata survives round-trip and TransientMetadataKey is stripped`() = runWithSimpleKeySerializer {
        val configuration = EnroController.savedStateConfiguration
        val serializer = NavigationKey.Instance.serializer(PolymorphicSerializer(NavigationKey::class))

        val original = NavigationKeyFixtures.SimpleKey().asInstance()
        original.metadata.set(PersistentTestMetadataKey, "I survive")
        original.metadata.set(TransientTestMetadataKey, "I do not survive")

        val encoded = encodeToSavedState(serializer, original, configuration)
        val decoded = decodeFromSavedState(serializer, encoded, configuration)

        assertEquals(
            expected = "I survive",
            actual = decoded.metadata.get(PersistentTestMetadataKey),
            message = "Persistent metadata should survive saved-state round-trip",
        )
        assertEquals(
            expected = "default-transient",
            actual = decoded.metadata.get(TransientTestMetadataKey),
            message = "TransientMetadataKey should not be persisted — the default should be returned after round-trip",
        )
    }
}
