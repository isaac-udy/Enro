package dev.enro.tests.application

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.annotations.NavigationDestination
import dev.enro.tests.application.compose.common.TitledColumn
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.asInstance
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import dev.enro.viewmodel.createEnroViewModel
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
class MultipleNestedContainerExample : NavigationKey {
    @Serializable
    class ChildKey(
        val name: String,
    ) : NavigationKey
}

@Composable
@NavigationDestination(MultipleNestedContainerExample::class)
fun NestedContainerExampleScreen() {
    val navigation = navigationHandle<MultipleNestedContainerExample>()
    val savedInstance = remember<MutableState<NavigationKey.Instance<*>>> {
        mutableStateOf(MultipleNestedContainerExample.ChildKey("Saved").asInstance())
    }
    val first = rememberNavigationContainer(
        backstack = listOf(MultipleNestedContainerExample.ChildKey("First").asInstance())
    )
    val second = rememberNavigationContainer(
        backstack = listOf(MultipleNestedContainerExample.ChildKey("Second").asInstance()),
    )
    val selectedState = remember { mutableStateOf(first) }
    TitledColumn("Nested Containers") {
        Row {
            Button(
                onClick = { selectedState.value = first },
                enabled = selectedState.value != first
            ) {
                Text(text = "First")
            }
            Button(
                onClick = { selectedState.value = second },
                enabled = selectedState.value != second
            ) {
                Text(text = "Second")
            }

            Button(
                onClick = {
                    selectedState.value.execute(NavigationOperation {
                        val saved = savedInstance.value
                        val current = it.last()
                        savedInstance.value = current
                        listOf(saved)
                    }
                    )
                },
            ) {
                Text(text = "Swap")
            }
        }
        Button(
            onClick = {
                navigation.open(MultipleNestedContainerExample())
            },
        ) {
            Text(text = "Open")
        }
        NavigationDisplay(
            state = selectedState.value
        )
    }
}

@NavigationDestination(MultipleNestedContainerExample.ChildKey::class)
@Composable
fun ChildKeyScreen() {
    val navigation = navigationHandle<MultipleNestedContainerExample.ChildKey>()
    val viewModel = viewModel<ComposeStabilityViewModel> {
        createEnroViewModel {
            ComposeStabilityViewModel(SavedStateHandle())
        }
    }
    val saved = rememberSaveable { Uuid.random() }
    val container = rememberNavigationContainer(
        backstack = listOf(EmptyKey().asInstance())
    )
    TitledColumn(
        title = "Child Key",
    ) {
        Text(text = "id: ${navigation.id}")
        Text(text = "name: ${navigation.key.name}")

        Text(text = "viewModel id: ${viewModel.id}")
        Text(text = "viewModel saved: ${viewModel.saveStateHandleId}")

        Text(text = "saved: $saved")
        Button(onClick = {
            container.execute(NavigationOperation.open(MultipleNestedContainerExample.ChildKey("inner").asInstance()))
        }) {
            Text(text = "Open Child")
        }
        NavigationDisplay(container)
    }
}