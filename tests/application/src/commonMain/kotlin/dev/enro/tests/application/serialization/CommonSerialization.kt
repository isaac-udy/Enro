package dev.enro.tests.application.serialization

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
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
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.close
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.tests.application.compose.common.TitledColumn
import dev.enro.ui.NavigationDisplay
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
    data class SerializableGenericNavigationKey<T : SerializableGenericNavigationKey.ContentData>(
        val typeName: String,
        val data: List<T>,
    ) : NavigationKey {
        @Serializable
        abstract class ContentData

        @Serializable
        sealed class GenericContentOne : ContentData() {
            @Serializable
            data class DataOne(
                val value: String,
            ) : GenericContentOne()

            @Serializable
            data class DataTwo(
                val value: Int,
            ) : GenericContentOne()
        }

        @Serializable
        sealed class GenericContentTwo : ContentData() {
            @Serializable
            data class DataOne(
                val value: String,
            ) : GenericContentTwo()

            @Serializable
            data class DataTwo(
                val value: Int,
            ) : GenericContentTwo()
        }

        companion object {
            fun createRandom(): SerializableGenericNavigationKey<*> {
                val selectedType = listOf(GenericContentOne::class, GenericContentTwo::class).random()
                @Suppress("RemoveExplicitTypeArguments") // being explicit
                return when (selectedType) {
                    GenericContentOne::class -> SerializableGenericNavigationKey<GenericContentOne>(
                        typeName = selectedType.qualifiedName!!,
                        data = List(Random.nextInt(10)) {
                            GenericContentOne.DataOne(Uuid.random().toString())
                        }.plus(
                            List(Random.nextInt(10)) {
                                GenericContentOne.DataTwo(Random.nextInt())
                            }
                        ).shuffled()
                    )
                    GenericContentTwo::class -> SerializableGenericNavigationKey<GenericContentTwo>(
                        typeName = selectedType.qualifiedName!!,
                        data = List(Random.nextInt(10)) {
                            GenericContentTwo.DataOne(Uuid.random().toString())
                        }.plus(
                            List(Random.nextInt(10)) {
                                GenericContentTwo.DataTwo(Random.nextInt())
                            }
                        ).shuffled()
                    )
                    else -> error("Unknown type")
                }
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
        backstack = emptyList()
    )

    TitledColumn(
        title = "Common Serialization",
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Button(
            onClick = {
                container.updateBackstack {
                    listOf(CommonSerialization.SerializableNavigationKey.createRandom().asInstance())
                }
            }
        ) {
            Text("Open Serializable")
        }

        Button(
            onClick = {
                container.updateBackstack {
                    listOf(CommonSerialization.SerializableGenericNavigationKey.createRandom().asInstance())
                }
            }
        ) {
            Text("Open Generic Serializable")
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


@NavigationDestination(CommonSerialization.SerializableGenericNavigationKey::class)
@Composable
fun GenericSerializableNavigationKeyScreen() {
    val navigation = navigationHandle<CommonSerialization.SerializableGenericNavigationKey<out CommonSerialization.SerializableGenericNavigationKey.ContentData>>()
    TitledColumn("Generic Serializable Navigation Key") {
        Text("Generic Data:")
        Text(
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.labelSmall,
            text = "Type: ${navigation.key.typeName}"
        )
        navigation.key.data.forEach {
            Text(
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.labelSmall,
                text = it.toString()
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
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
                            EnroController.jsonConfiguration.decodeFromString<NavigationKey.Instance<NavigationKey>>(
                                string = encodedData.data,
                            ).toString()
                        }

                        is CommonSerialization.SerializedData.NavigationInstanceSavedState -> {
                            decodeFromSavedState<NavigationKey.Instance<NavigationKey>>(
                                savedState = encodedData.data,
                                configuration = EnroController.savedStateConfiguration,
                            ).toString()
                        }
                    }
                }
            ) {
                Text("Decode")
            }
        } else {
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