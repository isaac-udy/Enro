package dev.enro.tests.application.serialization

import android.os.Parcelable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.savedstate.SavedState
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import androidx.savedstate.serialization.serializers.SavedStateSerializer
import dev.enro.EnroController
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.core.compose.navigationHandle
import dev.enro.core.requestClose
import dev.enro.open
import dev.enro.tests.application.compose.common.TitledColumn
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.destinations.EmptyNavigationKey
import dev.enro.ui.rememberNavigationContainer
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.util.*
import kotlin.random.Random

@Serializable
object AndroidSerialization : NavigationKey {
    @Parcelize
    data class ParcelableNavigationKey(
        val name: String,
        val parcelableData: ParcelableData,
        val generalData: ParcelableAndSerializableData,
    ) : Parcelable, NavigationKey {
        companion object {
            fun createRandom(): ParcelableNavigationKey {
                return ParcelableNavigationKey(
                    name = UUID.randomUUID().toString(),
                    parcelableData = ParcelableData(
                        string = UUID.randomUUID().toString(),
                        int = Random.nextInt(),
                        boolean = listOf(true, false).random(),
                        float = Random.nextFloat(),
                        double = Random.nextDouble(),
                        long = Random.nextLong(),
                    ),
                    generalData = ParcelableAndSerializableData(
                        string = UUID.randomUUID().toString(),
                        int = Random.nextInt(),
                        boolean = listOf(true, false).random(),
                        float = Random.nextFloat(),
                        double = Random.nextDouble(),
                        long = Random.nextLong(),
                    )
                )
            }
        }
    }

    @Serializable
    data class SerializableNavigationKey(
        val name: String,
        val serializableData: SerializableData,
        val generalData: ParcelableAndSerializableData,
    ) : NavigationKey {
        companion object {
            fun createRandom(): SerializableNavigationKey {
                return SerializableNavigationKey(
                    name = UUID.randomUUID().toString(),
                    serializableData = SerializableData(
                        string = UUID.randomUUID().toString(),
                        int = Random.nextInt(),
                        boolean = listOf(true, false).random(),
                        float = Random.nextFloat(),
                        double = Random.nextDouble(),
                        long = Random.nextLong(),
                    ),
                    generalData = ParcelableAndSerializableData(
                        string = UUID.randomUUID().toString(),
                        int = Random.nextInt(),
                        boolean = listOf(true, false).random(),
                        float = Random.nextFloat(),
                        double = Random.nextDouble(),
                        long = Random.nextLong(),
                    )
                )
            }
        }
    }

    @Serializable
    class DisplaySerializedData(
        val serializedData: SerializedData,
    ) : NavigationKey

    @Serializable
    sealed interface SerializedData {
        @Serializable
        class NavigationKeyJson(
            val data: String,
        ) : SerializedData

        @Serializable
        class NavigationKeySavedState(
            val data: @Serializable(with = SavedStateSerializer::class) SavedState,
        ) : SerializedData

        @Serializable
        class NavigationInstanceJson(
            val data: String,
        ) : SerializedData

        @Serializable
        class NavigationInstanceSavedState(
            val data: @Serializable(with = SavedStateSerializer::class) SavedState,
        ) : SerializedData
    }
}

@Parcelize
data class ParcelableData(
    val string: String,
    val int: Int,
    val boolean: Boolean,
    val float: Float,
    val double: Double,
    val long: Long,
) : Parcelable

@Serializable
data class SerializableData(
    val string: String,
    val int: Int,
    val boolean: Boolean,
    val float: Float,
    val double: Double,
    val long: Long,
) : NavigationKey

@Parcelize
@Serializable
data class ParcelableAndSerializableData(
    val string: String,
    val int: Int,
    val boolean: Boolean,
    val float: Float,
    val double: Double,
    val long: Long,
) : Parcelable, NavigationKey


@NavigationDestination(AndroidSerialization::class)
@Composable
fun AndroidSerializationScreen() {
    val container = rememberNavigationContainer(
        backstack = listOf(EmptyNavigationKey.asInstance()),
    )

    TitledColumn("Android Serialization") {
        Button(
            onClick = {
                container.execute(NavigationOperation {
                    listOf(AndroidSerialization.ParcelableNavigationKey.createRandom().asInstance())
                })
            }
        ) {
            Text("Open Parcelable")
        }
        Button(
            onClick = {
                container.execute(NavigationOperation {
                    listOf(AndroidSerialization.SerializableNavigationKey.createRandom().asInstance())
                })
            }
        ) {
            Text("Open Serializable")
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            NavigationDisplay(container)
        }
    }
}


@NavigationDestination(AndroidSerialization.ParcelableNavigationKey::class)
@Composable
fun ParcelableNavigationKeyScreen() {
    val navigation = navigationHandle<AndroidSerialization.ParcelableNavigationKey>()

    TitledColumn("Parcelable") {
        Button(
            onClick = {
                navigation.open(
                    AndroidSerialization.DisplaySerializedData(
                        AndroidSerialization.SerializedData.NavigationKeyJson(
                            data = EnroController.jsonConfiguration.encodeToString(navigation.key)
                        )
                    )
                )
            }
        ) {
            Text("NavigationKey as Json")
        }

        Button(
            onClick = {
                // TODO the encodeToSavedState doesn't work here unless a type is specified
                // encodeToSavedState<NavigationKey>(...) works, but encodeToSavedState(...) doesn't
                // and will crash; need to understand why that's happening
                navigation.open(
                    AndroidSerialization.DisplaySerializedData(
                        AndroidSerialization.SerializedData.NavigationKeySavedState(
                            data = encodeToSavedState<NavigationKey>(navigation.key, EnroController.savedStateConfiguration)
                        )
                    )
                )
            }
        ) {
            Text("NavigationKey as SavedState")
        }

        Button(
            onClick = {
                navigation.open(
                    AndroidSerialization.DisplaySerializedData(
                        AndroidSerialization.SerializedData.NavigationInstanceJson(
                            data = EnroController.jsonConfiguration.encodeToString(navigation.instance)
                        )
                    )
                )
            }
        ) {
            Text("NavigationInstruction as Json")
        }

        Button(
            onClick = {
                navigation.open(
                    AndroidSerialization.DisplaySerializedData(
                        AndroidSerialization.SerializedData.NavigationInstanceSavedState(
                            data = encodeToSavedState(navigation.instance, EnroController.savedStateConfiguration)
                        )
                    )
                )
            }
        ) {
            Text("NavigationInstruction as SavedState")
        }
    }
}

@NavigationDestination(AndroidSerialization.SerializableNavigationKey::class)
@Composable
fun SerializableNavigationKeyScreen() {
    val navigation = navigationHandle<AndroidSerialization.SerializableNavigationKey>()
    TitledColumn("Serializable") {
        Button(
            onClick = {
                navigation.open(
                    AndroidSerialization.DisplaySerializedData(
                        AndroidSerialization.SerializedData.NavigationKeyJson(
                            data = EnroController.jsonConfiguration.encodeToString(navigation.key)
                        )
                    )
                )
            }
        ) {
            Text("NavigationKey as Json")
        }

        Button(
            onClick = {
                navigation.open(
                    AndroidSerialization.DisplaySerializedData(
                        AndroidSerialization.SerializedData.NavigationKeySavedState(
                            data = encodeToSavedState<NavigationKey>(navigation.key, EnroController.savedStateConfiguration)
                        )
                    )
                )
            }
        ) {
            Text("NavigationKey as SavedState")
        }

        Button(
            onClick = {
                navigation.open(
                    AndroidSerialization.DisplaySerializedData(
                        AndroidSerialization.SerializedData.NavigationInstanceJson(
                            data = EnroController.jsonConfiguration.encodeToString(navigation.instance)
                        )
                    )
                )
            }
        ) {
            Text("NavigationInstruction as Json")
        }

        Button(
            onClick = {
                navigation.open(
                    AndroidSerialization.DisplaySerializedData(
                        AndroidSerialization.SerializedData.NavigationInstanceSavedState(
                            data = encodeToSavedState(navigation.instance, EnroController.savedStateConfiguration)
                        )
                    )
                )
            }
        ) {
            Text("NavigationInstruction as SavedState")
        }
    }
}

@NavigationDestination(AndroidSerialization.DisplaySerializedData::class)
@Composable
fun DisplaySerializedDataScreen() {
    val navigation = navigationHandle<AndroidSerialization.DisplaySerializedData>()
    val encodedData = navigation.key.serializedData
    var decodedData by remember { mutableStateOf<String?>(null) }
    TitledColumn(
        title = "Serialized Data",
        modifier = Modifier.verticalScroll(rememberScrollState()),
    ) {
        if (decodedData == null) {
            Text("Encoded:")
            val encodedString = when (encodedData) {
                is AndroidSerialization.SerializedData.NavigationInstanceJson -> encodedData.data.toString()
                is AndroidSerialization.SerializedData.NavigationInstanceSavedState -> encodedData.data.toString()
                is AndroidSerialization.SerializedData.NavigationKeyJson -> encodedData.data.toString()
                is AndroidSerialization.SerializedData.NavigationKeySavedState -> encodedData.data.toString()
            }
            Text(encodedString)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    decodedData = when (encodedData) {
                        is AndroidSerialization.SerializedData.NavigationKeyJson -> {
                            EnroController.jsonConfiguration.decodeFromString<NavigationKey>(
                                string = encodedData.data,
                            ).toString()
                        }
                        is AndroidSerialization.SerializedData.NavigationKeySavedState -> {
                            decodeFromSavedState<NavigationKey>(
                                savedState = encodedData.data,
                                configuration = EnroController.savedStateConfiguration,
                            ).toString()
                        }
                        is AndroidSerialization.SerializedData.NavigationInstanceJson -> {
                            EnroController.jsonConfiguration.decodeFromString<NavigationKey.Instance<*>>(
                                string = encodedData.data,
                            ).toString()
                        }
                        is AndroidSerialization.SerializedData.NavigationInstanceSavedState -> {
                            decodeFromSavedState<NavigationKey.Instance<*>>(
                                savedState = encodedData.data,
                                configuration = EnroController.savedStateConfiguration,
                            ).toString()
                        }
                    }
                }
            ) {
                Text("Decode")
            }
        }
        else {
            Text("Decoded:")
            Text(decodedData!!)
            Spacer(modifier = Modifier.height(16.dp))
        }
        Button(
            onClick = {
                navigation.requestClose()
            }
        ) {
            Text("Close")
        }
    }
}