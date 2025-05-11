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
import dev.enro.annotations.NavigationDestination
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.asPush
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.backstackOf
import dev.enro.core.container.setBackstack
import dev.enro.core.controller.NavigationController
import dev.enro.core.push
import dev.enro.core.requestClose
import dev.enro.tests.application.compose.common.TitledColumn
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlin.random.Random
import kotlin.uuid.Uuid

@Serializable
object CommonSerialization : NavigationKey.SupportsPush {
    @Serializable
    data class SerializableNavigationKey(
        val name: String,
        val serializableData: CommonSerializableData,
    ) : NavigationKey.SupportsPush {
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
    ) : NavigationKey.SupportsPush

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
        class NavigationInstructionJson(
            val data: String,
        ) : SerializedData

        @Serializable
        class NavigationInstructionSavedState(
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
) : NavigationKey.SupportsPush

@NavigationDestination(CommonSerialization::class)
@Composable
fun CommonSerializationScreen() {
    val container = rememberNavigationContainer(
        emptyBehavior = EmptyBehavior.CloseParent,
    )

    TitledColumn("Common Serialization") {
        Button(
            onClick = {
                container.setBackstack {
                    backstackOf(CommonSerialization.SerializableNavigationKey.createRandom().asPush())
                }
            }
        ) {
            Text("Open Serializable")
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            container.Render()
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
                navigation.push(
                    CommonSerialization.DisplaySerializedData(
                        CommonSerialization.SerializedData.NavigationKeyJson(
                            data = NavigationController.jsonConfiguration.encodeToString(
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
                navigation.push(
                    CommonSerialization.DisplaySerializedData(
                        CommonSerialization.SerializedData.NavigationKeySavedState(
                            data = encodeToSavedState<NavigationKey>(
                                PolymorphicSerializer(NavigationKey::class),
                                navigation.key,
                                NavigationController.savedStateConfiguration
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
                navigation.push(
                    CommonSerialization.DisplaySerializedData(
                        CommonSerialization.SerializedData.NavigationInstructionJson(
                            data = NavigationController.jsonConfiguration.encodeToString(navigation.instruction)
                        )
                    )
                )
            }
        ) {
            Text("NavigationInstruction as Json")
        }

        Button(
            onClick = {
                navigation.push(
                    CommonSerialization.DisplaySerializedData(
                        CommonSerialization.SerializedData.NavigationInstructionSavedState(
                            data = encodeToSavedState(navigation.instruction, NavigationController.savedStateConfiguration)
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
                is CommonSerialization.SerializedData.NavigationInstructionJson -> encodedData.data.toString()
                is CommonSerialization.SerializedData.NavigationInstructionSavedState -> encodedData.data.toString()
                is CommonSerialization.SerializedData.NavigationKeyJson -> encodedData.data.toString()
                is CommonSerialization.SerializedData.NavigationKeySavedState -> encodedData.data.toString()
            }
            Text(encodedString)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    decodedData = when (encodedData) {
                        is CommonSerialization.SerializedData.NavigationKeyJson -> {
                            NavigationController.jsonConfiguration.decodeFromString(
                                deserializer = PolymorphicSerializer(NavigationKey::class),
                                string = encodedData.data,
                            ).toString()
                        }
                        is CommonSerialization.SerializedData.NavigationKeySavedState -> {
                            decodeFromSavedState(
                                deserializer = PolymorphicSerializer(NavigationKey::class),
                                savedState = encodedData.data,
                                configuration = NavigationController.savedStateConfiguration,
                            ).toString()
                        }
                        is CommonSerialization.SerializedData.NavigationInstructionJson -> {
                            NavigationController.jsonConfiguration.decodeFromString<AnyOpenInstruction>(
                                string = encodedData.data,
                            ).toString()
                        }
                        is CommonSerialization.SerializedData.NavigationInstructionSavedState -> {
                            decodeFromSavedState<AnyOpenInstruction>(
                                savedState = encodedData.data,
                                configuration = NavigationController.savedStateConfiguration,
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