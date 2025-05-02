package dev.enro.tests.application

import android.os.Parcel
import android.util.Base64
import androidx.savedstate.SavedState
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.asPush
import dev.enro.core.controller.NavigationController
import dev.enro.tests.application.serialization.ParcelableAndSerializableData
import dev.enro.tests.application.serialization.ParcelableData
import dev.enro.tests.application.serialization.SerializableData
import dev.enro.tests.application.serialization.AndroidSerialization
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID
import kotlin.random.Random

class AndroidSerializationTests {

    private val parcelableNavigationKey = AndroidSerialization.ParcelableNavigationKey.createRandom()
    private val serializableNavigationKey = AndroidSerialization.SerializableNavigationKey.createRandom()

    @Test
    fun testParcelableCanBeCycledToParcelWithExplicitType() {
        val savedState = encodeToSavedState<AndroidSerialization.ParcelableNavigationKey>(
            value = parcelableNavigationKey,
            configuration = NavigationController.savedStateConfiguration,
        )
        val encodedParcelableNavigationKey = cycleSavedState(
            data = savedState,
        )
        val decodedParcelableNavigationKey = decodeFromSavedState<AndroidSerialization.ParcelableNavigationKey>(
            savedState = encodedParcelableNavigationKey,
            configuration = NavigationController.savedStateConfiguration
        )
        assertEquals(parcelableNavigationKey, decodedParcelableNavigationKey)
    }

    @Test
    fun testParcelableCanBeCycledToParcelWithGenericType() {
        val savedState = encodeToSavedState<NavigationKey>(
            value = parcelableNavigationKey,
            configuration = NavigationController.savedStateConfiguration,
        )
        val encodedParcelableNavigationKey = cycleSavedState(
            data = savedState,
        )
        val decodedParcelableNavigationKey = decodeFromSavedState<NavigationKey>(
            savedState = encodedParcelableNavigationKey,
            configuration = NavigationController.savedStateConfiguration
        )
        assertEquals(parcelableNavigationKey, decodedParcelableNavigationKey)
    }

    @Test
    fun testSerializableCanBeCycledToParcelWithExplicitType() {
        val savedState = encodeToSavedState<AndroidSerialization.SerializableNavigationKey>(
            value = serializableNavigationKey,
            configuration = NavigationController.savedStateConfiguration,
        )
        val encodedSerializableNavigationKey = cycleSavedState(
            data = savedState,
        )
        val decodedSerializableNavigationKey = decodeFromSavedState<AndroidSerialization.SerializableNavigationKey>(
            savedState = encodedSerializableNavigationKey,
            configuration = NavigationController.savedStateConfiguration
        )
        assertEquals(serializableNavigationKey, decodedSerializableNavigationKey)
    }

    @Test
    fun testSerializableCanBeCycledToParcelWithGenericType() {
        val savedState = encodeToSavedState<NavigationKey>(
            value = serializableNavigationKey,
            configuration = NavigationController.savedStateConfiguration,
        )
        val encodedSerializableNavigationKey = cycleSavedState(
            data = savedState,
        )
        val decodedSerializableNavigationKey = decodeFromSavedState<NavigationKey>(
            savedState = encodedSerializableNavigationKey,
            configuration = NavigationController.savedStateConfiguration
        )
        assertEquals(serializableNavigationKey, decodedSerializableNavigationKey)
    }

    @Test
    fun testParcelableCanBeCycledToJsonWithExplicitType() {
        val jsonString = NavigationController.jsonConfiguration.encodeToString<AndroidSerialization.ParcelableNavigationKey>(
            parcelableNavigationKey,
        )
        val decodedJson = NavigationController.jsonConfiguration.decodeFromString<AndroidSerialization.ParcelableNavigationKey>(
            jsonString,
        )
        assertEquals(parcelableNavigationKey, decodedJson)
    }

    @Test
    fun testParcelableCanBeCycledToJsonWithGenericType() {
        val jsonString = NavigationController.jsonConfiguration.encodeToString<NavigationKey>(
            parcelableNavigationKey,
        )
        val decodedJson = NavigationController.jsonConfiguration.decodeFromString<NavigationKey>(
            jsonString,
        )
        assertEquals(parcelableNavigationKey, decodedJson)
    }

    @Test
    fun testSerializableCanBeCycledToJsonWithExplicitType() {
        val jsonString = NavigationController.jsonConfiguration.encodeToString<AndroidSerialization.SerializableNavigationKey>(
            serializableNavigationKey,
        )
        val decodedJson = NavigationController.jsonConfiguration.decodeFromString<AndroidSerialization.SerializableNavigationKey>(
            jsonString,
        )
        assertEquals(serializableNavigationKey, decodedJson)
    }

    @Test
    fun testSerializableCanBeCycledToJsonWithGenericType() {
        val jsonString = NavigationController.jsonConfiguration.encodeToString<NavigationKey>(
            serializableNavigationKey,
        )
        val decodedJson = NavigationController.jsonConfiguration.decodeFromString<NavigationKey>(
            jsonString,
        )
        assertEquals(serializableNavigationKey, decodedJson)
    }

    @Test
    fun testNavigationInstructionParcelableKeyAndExtrasCanBeSerialized() {
        val instruction = parcelableNavigationKey.asPush()
            .apply {
                extras.put("string",UUID.randomUUID().toString())
                extras.put("int",Random.nextInt())
                extras.put("boolean",listOf(true, false).random())
                extras.put("float",Random.nextFloat())
                extras.put("double",Random.nextDouble())
                extras.put("long",Random.nextLong())
                extras.put("list", listOf(1,2,3,4,5))
                extras.put("listOfKeys", listOf(parcelableNavigationKey, serializableNavigationKey))
                extras.put("set", setOf(1,2,3,4))
                extras.put("map", mapOf("key" to "value"))
                extras.put("mapOfKeys", mapOf(parcelableNavigationKey to serializableNavigationKey))
            }
        val cycled = cycleSavedState(
            data = encodeToSavedState(
                value = instruction,
                configuration = NavigationController.savedStateConfiguration,
            ),
        )
        val decoded = decodeFromSavedState<NavigationInstruction.Open<out NavigationDirection>>(
            savedState = cycled,
            configuration = NavigationController.savedStateConfiguration
        )
        assertEquals(instruction, decoded)
    }

    @Test
    fun testNavigationInstructionSerializableKeyAndExtrasCanBeSerialized() {
        val instruction = serializableNavigationKey.asPush()
            .apply {
                extras.put("string", UUID.randomUUID().toString())
                extras.put("int", Random.nextInt())
                extras.put("boolean", listOf(true, false).random())
                extras.put("float", Random.nextFloat())
                extras.put("double", Random.nextDouble())
                extras.put("long", Random.nextLong())
                extras.put("list", listOf(1, 2, 3, 4, 5))
                extras.put("listOfKeys", listOf(parcelableNavigationKey, serializableNavigationKey))
                extras.put("set", setOf(1, 2, 3, 4))
                extras.put("map", mapOf("key" to "value"))
                extras.put("mapOfKeys", mapOf(parcelableNavigationKey to serializableNavigationKey))
            }
        val cycled = cycleSavedState(
            data = encodeToSavedState(
                value = instruction,
                configuration = NavigationController.savedStateConfiguration,
            ),
        )
        val decoded = decodeFromSavedState<NavigationInstruction.Open<out NavigationDirection>>(
            savedState = cycled,
            configuration = NavigationController.savedStateConfiguration
        )
        assertEquals(instruction, decoded)
    }
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
