package dev.enro.tests.application.serialization

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
import dev.enro.close
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.tests.application.compose.common.TitledColumn
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.destinations.EmptyNavigationKey
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlin.random.Random
import kotlin.uuid.Uuid

@Serializable
object CommonSerialization : NavigationKey {
    @Serializable
    data class SerializableNavigationKey(
        val name: String,
        val serializableData: CommonSerializableData,
    ) : NavigationKey {
        companion object {
            fun createRandom(): SerializableNavigationKey {
                return SerializableNavigationKey(
                    name = Uuid.random().toString(),
                    serializableData = CommonSerializableData(
                        string = Uuid.random().toString(),
                        int = Random.nextInt(),
                        boolean = listOf(true, false).random(),
                        float = Random.nextFloat(),
                        double = Random.nextDouble(),
                        long = Random.nextLong(),
                    ),
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

@Serializable
data class CommonSerializableData(
    val string: String,
    val int: Int,
    val boolean: Boolean,
    val float: Float,
    val double: Double,
    val long: Long,
) : NavigationKey

@NavigationDestination(CommonSerialization::class)
@Composable
fun CommonSerializationScreen() {
    val container = rememberNavigationContainer(
        backstack = listOf(EmptyNavigationKey.asInstance())
    )

    TitledColumn("Common Serialization") {
        Button(
            onClick = {
                container.execute(NavigationOperation {
                    listOf(CommonSerialization.SerializableNavigationKey.createRandom().asInstance())
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

@NavigationDestination(CommonSerialization.SerializableNavigationKey::class)
@Composable
fun CommonSerializableNavigationKeyScreen() {
    val navigation = navigationHandle<CommonSerialization.SerializableNavigationKey>()
    TitledColumn("Serializable") {
        Button(
            onClick = {
                navigation.open(
                    CommonSerialization.DisplaySerializedData(
                        CommonSerialization.SerializedData.NavigationKeyJson(
                            data = EnroController.jsonConfiguration.encodeToString(
                                PolymorphicSerializer(NavigationKey::class),
                                navigation.key
                            )
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
                    CommonSerialization.DisplaySerializedData(
                        CommonSerialization.SerializedData.NavigationKeySavedState(
                            data = encodeToSavedState<NavigationKey>(
                                PolymorphicSerializer(NavigationKey::class),
                                navigation.key,
                                EnroController.savedStateConfiguration
                            )
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
                    CommonSerialization.DisplaySerializedData(
                        CommonSerialization.SerializedData.NavigationInstanceJson(
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
                    CommonSerialization.DisplaySerializedData(
                        CommonSerialization.SerializedData.NavigationInstanceSavedState(
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

@NavigationDestination(CommonSerialization.DisplaySerializedData::class)
@Composable
fun CommonDisplaySerializedDataScreen() {
    val navigation = navigationHandle<CommonSerialization.DisplaySerializedData>()
    val encodedData = navigation.key.serializedData
    var decodedData by remember { mutableStateOf<String?>(null) }
    TitledColumn(
        title = "Serialized Data",
        modifier = Modifier.verticalScroll(rememberScrollState()),
    ) {
        if (decodedData == null) {
            Text("Encoded:")
            val encodedString = when (encodedData) {
                is CommonSerialization.SerializedData.NavigationInstanceJson -> encodedData.data.toString()
                is CommonSerialization.SerializedData.NavigationInstanceSavedState -> encodedData.data.toString()
                is CommonSerialization.SerializedData.NavigationKeyJson -> encodedData.data.toString()
                is CommonSerialization.SerializedData.NavigationKeySavedState -> encodedData.data.toString()
            }
            Text(encodedString)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    decodedData = when (encodedData) {
                        is CommonSerialization.SerializedData.NavigationKeyJson -> {
                            EnroController.jsonConfiguration.decodeFromString(
                                deserializer = PolymorphicSerializer(NavigationKey::class),
                                string = encodedData.data,
                            ).toString()
                        }
                        is CommonSerialization.SerializedData.NavigationKeySavedState -> {
                            decodeFromSavedState(
                                deserializer = PolymorphicSerializer(NavigationKey::class),
                                savedState = encodedData.data,
                                configuration = EnroController.savedStateConfiguration,
                            ).toString()
                        }
                        is CommonSerialization.SerializedData.NavigationInstanceJson -> {
                            EnroController.jsonConfiguration.decodeFromString<NavigationKey.Instance<*>>(
                                string = encodedData.data,
                            ).toString()
                        }
                        is CommonSerialization.SerializedData.NavigationInstanceSavedState -> {
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
                navigation.close()
            }
        ) {
            Text("Close")
        }
    }
}