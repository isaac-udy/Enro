package dev.enro.tests.application

import android.os.Parcel
import android.util.Base64
import androidx.savedstate.SavedState
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import dev.enro.EnroController
import dev.enro.NavigationKey
import dev.enro.asInstance
import dev.enro.tests.application.serialization.AndroidSerialization
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*
import kotlin.random.Random

class AndroidSerializationTests {

    private val parcelableNavigationKey = AndroidSerialization.ParcelableNavigationKey.createRandom()
    private val serializableNavigationKey = AndroidSerialization.SerializableNavigationKey.createRandom()

    @Test
    fun testParcelableCanBeCycledToParcelWithExplicitType() {
        val savedState = encodeToSavedState<AndroidSerialization.ParcelableNavigationKey>(
            value = parcelableNavigationKey,
            configuration = EnroController.savedStateConfiguration,
        )
        val encodedParcelableNavigationKey = cycleSavedState(
            data = savedState,
        )
        val decodedParcelableNavigationKey = decodeFromSavedState<AndroidSerialization.ParcelableNavigationKey>(
            savedState = encodedParcelableNavigationKey,
            configuration = EnroController.savedStateConfiguration
        )
        assertEquals(parcelableNavigationKey, decodedParcelableNavigationKey)
    }

    @Test
    fun testParcelableCanBeCycledToParcelWithGenericType() {
        val savedState = encodeToSavedState<NavigationKey>(
            value = parcelableNavigationKey,
            configuration = EnroController.savedStateConfiguration,
        )
        val encodedParcelableNavigationKey = cycleSavedState(
            data = savedState,
        )
        val decodedParcelableNavigationKey = decodeFromSavedState<NavigationKey>(
            savedState = encodedParcelableNavigationKey,
            configuration = EnroController.savedStateConfiguration
        )
        assertEquals(parcelableNavigationKey, decodedParcelableNavigationKey)
    }

    @Test
    fun testSerializableCanBeCycledToParcelWithExplicitType() {
        val savedState = encodeToSavedState<AndroidSerialization.SerializableNavigationKey>(
            value = serializableNavigationKey,
            configuration = EnroController.savedStateConfiguration,
        )
        val encodedSerializableNavigationKey = cycleSavedState(
            data = savedState,
        )
        val decodedSerializableNavigationKey = decodeFromSavedState<AndroidSerialization.SerializableNavigationKey>(
            savedState = encodedSerializableNavigationKey,
            configuration = EnroController.savedStateConfiguration
        )
        assertEquals(serializableNavigationKey, decodedSerializableNavigationKey)
    }

    @Test
    fun testSerializableCanBeCycledToParcelWithGenericType() {
        val savedState = encodeToSavedState<NavigationKey>(
            value = serializableNavigationKey,
            configuration = EnroController.savedStateConfiguration,
        )
        val encodedSerializableNavigationKey = cycleSavedState(
            data = savedState,
        )
        val decodedSerializableNavigationKey = decodeFromSavedState<NavigationKey>(
            savedState = encodedSerializableNavigationKey,
            configuration = EnroController.savedStateConfiguration
        )
        assertEquals(serializableNavigationKey, decodedSerializableNavigationKey)
    }

    @Test
    fun testParcelableCanBeCycledToJsonWithExplicitType() {
        val jsonString = EnroController.jsonConfiguration.encodeToString<AndroidSerialization.ParcelableNavigationKey>(
            parcelableNavigationKey,
        )
        val decodedJson = EnroController.jsonConfiguration.decodeFromString<AndroidSerialization.ParcelableNavigationKey>(
            jsonString,
        )
        assertEquals(parcelableNavigationKey, decodedJson)
    }

    @Test
    fun testParcelableCanBeCycledToJsonWithGenericType() {
        val jsonString = EnroController.jsonConfiguration.encodeToString<NavigationKey>(
            parcelableNavigationKey,
        )
        val decodedJson = EnroController.jsonConfiguration.decodeFromString<NavigationKey>(
            jsonString,
        )
        assertEquals(parcelableNavigationKey, decodedJson)
    }

    @Test
    fun testSerializableCanBeCycledToJsonWithExplicitType() {
        val jsonString = EnroController.jsonConfiguration.encodeToString<AndroidSerialization.SerializableNavigationKey>(
            serializableNavigationKey,
        )
        val decodedJson = EnroController.jsonConfiguration.decodeFromString<AndroidSerialization.SerializableNavigationKey>(
            jsonString,
        )
        assertEquals(serializableNavigationKey, decodedJson)
    }

    @Test
    fun testSerializableCanBeCycledToJsonWithGenericType() {
        val jsonString = EnroController.jsonConfiguration.encodeToString<NavigationKey>(
            serializableNavigationKey,
        )
        val decodedJson = EnroController.jsonConfiguration.decodeFromString<NavigationKey>(
            jsonString,
        )
        assertEquals(serializableNavigationKey, decodedJson)
    }

    @Test
    fun testNavigationInstructionParcelableKeyAndExtrasCanBeSerialized() {
        val instruction = parcelableNavigationKey.asInstance()
            .apply {
                metadata.set(MetadataString, UUID.randomUUID().toString())
                metadata.set(MetadataInt, Random.nextInt())
                metadata.set(MetadataBoolean, listOf(true, false).random())
                metadata.set(MetadataFloat, Random.nextFloat())
                metadata.set(MetadataDouble, Random.nextDouble())
                metadata.set(MetadataLong, Random.nextLong())
                metadata.set(MetadataList, listOf(1, 2, 3, 4, 5))
                metadata.set(MetadataListOfKeys, listOf(parcelableNavigationKey, serializableNavigationKey))
                metadata.set(MetadataSet, setOf(1, 2, 3, 4))
                metadata.set(MetadataMap, mapOf("key" to "value"))
                metadata.set(MetadataMapOfKeys, mapOf(parcelableNavigationKey to serializableNavigationKey))
            }
        val cycled = cycleSavedState(
            data = encodeToSavedState(
                value = instruction,
                configuration = EnroController.savedStateConfiguration,
            ),
        )
        val decoded = decodeFromSavedState<NavigationKey.Instance<NavigationKey>>(
            savedState = cycled,
            configuration = EnroController.savedStateConfiguration
        )
        assertEquals(instruction, decoded)
    }

    @Test
    fun testNavigationInstructionSerializableKeyAndExtrasCanBeSerialized() {
        val instruction = serializableNavigationKey.asInstance()
            .apply {
                metadata.set(MetadataString, UUID.randomUUID().toString())
                metadata.set(MetadataInt, Random.nextInt())
                metadata.set(MetadataBoolean, listOf(true, false).random())
                metadata.set(MetadataFloat, Random.nextFloat())
                metadata.set(MetadataDouble, Random.nextDouble())
                metadata.set(MetadataLong, Random.nextLong())
                metadata.set(MetadataList, listOf(1, 2, 3, 4, 5))
                metadata.set(MetadataListOfKeys, listOf(parcelableNavigationKey, serializableNavigationKey))
                metadata.set(MetadataSet, setOf(1, 2, 3, 4))
                metadata.set(MetadataMap, mapOf("key" to "value"))
                metadata.set(MetadataMapOfKeys, mapOf(parcelableNavigationKey to serializableNavigationKey))
            }
        val cycled = cycleSavedState(
            data = encodeToSavedState(
                value = instruction,
                configuration = EnroController.savedStateConfiguration,
            ),
        )
        val decoded = decodeFromSavedState<NavigationKey.Instance<NavigationKey>>(
            savedState = cycled,
            configuration = EnroController.savedStateConfiguration
        )
        assertEquals(instruction, decoded)
    }

    object MetadataString : NavigationKey.MetadataKey<String?>(null)
    object MetadataInt : NavigationKey.MetadataKey<Int?>(null)
    object MetadataBoolean : NavigationKey.MetadataKey<Boolean?>(null)
    object MetadataFloat : NavigationKey.MetadataKey<Float?>(null)
    object MetadataDouble : NavigationKey.MetadataKey<Double?>(null)
    object MetadataLong : NavigationKey.MetadataKey<Long?>(null)
    object MetadataList : NavigationKey.MetadataKey<List<*>?>(null)
    object MetadataListOfKeys : NavigationKey.MetadataKey<List<NavigationKey>?>(null)
    object MetadataSet : NavigationKey.MetadataKey<Set<*>?>(null)
    object MetadataMap : NavigationKey.MetadataKey<Map<*, *>?>(null)
    object MetadataMapOfKeys : NavigationKey.MetadataKey<Map<NavigationKey, NavigationKey>?>(null)
}

/**
 * This function is used to cycle the saved state of a [SavedState] object through a Parcel,
 * which involves writing the SavedState data into a Parcel and then reading it back out.
 * This is important to ensure that the data is correctly marshalled and unmarshalled.
 */
fun cycleSavedState(
    data: SavedState,
): SavedState {
    val parcel = Parcel.obtain()
    data.writeToParcel(parcel, 0)

    val base64Encoded = Base64.encodeToString(parcel.marshall(), Base64.DEFAULT)
    val base64Decoded = Base64.decode(base64Encoded, Base64.DEFAULT)

    val savedParcel = Parcel.obtain().apply {
        unmarshall(base64Decoded, 0, base64Decoded.size)
    }
    savedParcel.setDataPosition(0)
    val readState = savedParcel.readBundle(AndroidSerializationTests::class.java.classLoader)
    savedParcel.recycle()
    return readState as SavedState
}
