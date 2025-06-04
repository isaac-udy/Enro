package dev.enro.tests.application

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.annotations.NavigationComponent
import dev.enro.annotations.NavigationDestination
import dev.enro.tests.application.compose.common.TitledColumn
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.asInstance
import dev.enro.close
import dev.enro.closeWithoutCallback
import dev.enro.complete
import dev.enro.completeFrom
import dev.enro.configure
import dev.enro.controller.NavigationControllerConfiguration
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.result.flow.registerForFlowResult
import dev.enro.result.flow.rememberNavigationContainerForFlow
import dev.enro.result.open
import dev.enro.result.registerForNavigationResult
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.destinations.syntheticDestination
import dev.enro.ui.navigationDestination
import dev.enro.ui.rememberNavigationContainer
import dev.enro.ui.scenes.DialogSceneStrategy
import dev.enro.ui.scenes.DirectOverlaySceneStrategy
import dev.enro.viewmodel.createEnroViewModel
import kotlinx.serialization.Serializable


@NavigationComponent
object NavigationComponent : NavigationControllerConfiguration()


@Serializable
class ListKey : NavigationKey

@NavigationDestination(ListKey::class)
val listDestination = navigationDestination<ListKey> {
    val navigation = navigationHandle<NavigationKey>()
    val result = rememberSaveable { mutableStateOf("") }
    val stringResultChannel = registerForNavigationResult<String>(
        onClosed = {
            result.value = "Closed string"
        }
    ) {
        result.value = it
    }
    val flowResultChannel = registerForNavigationResult<Pair<String, String>>(
        onClosed = {
            result.value = "Closed flow"
        }
    ) {
        result.value = "Flow: ${it.first} ${it.second}"
    }
    val resultChannel = registerForNavigationResult(
        onClosed = {
            result.value = "Closed ${key::class.simpleName}"
        }
    ) {
        result.value = "Completed ${key::class.simpleName}"
    }

    TitledColumn(
        title = "List",
        modifier = Modifier.verticalScroll(rememberScrollState()),
    ) {
        Text("Result: ${result.value}")
        Button(onClick = {
            stringResultChannel.open(ResultKey())
        }) {
            Text("Get Result")
        }
        Button(onClick = {
            navigation.open(SyntheticKey("Hello Synthetics"))
        }) {
            Text("Synthetic")
        }
        Button(onClick = {
            resultChannel.open(DialogKey())
        }) {
            Text("Dialog")
        }
        Button(onClick = {
            resultChannel.open(DirectDialogKey())
        }) {
            Text("Direct Dialog")
        }
        Button(onClick = {
            resultChannel.open(DirectButtonKey())
        }) {
            Text("Direct Button")
        }
        Button(onClick = {
            resultChannel.open(DirectBottomSheetKey())
        }) {
            Text("Direct Bottom Sheet")
        }
        Button(onClick = {
            flowResultChannel.open(FlowKey())
        }) {
            Text("Flow")
        }
        Button(onClick = {
            resultChannel.open(NestedKey())
        }) {
            Text("Nested")
        }
        Button(onClick = {
            stringResultChannel.open(ScreenWithViewModelKey())
        }) {
            Text("ViewModel")
        }
        repeat(3) {
            Button(onClick = {
                resultChannel.open(DetailKey(it.toString()))
            }) {
                Text("Open Detail $it")
            }
        }
    }
}

@Serializable
class DetailKey(
    val id: String,
) : NavigationKey

@NavigationDestination(DetailKey::class)
val detailDestination = navigationDestination<DetailKey> {
    val navigation = navigationHandle<DetailKey>()
    TitledColumn("Details") {
        Text("id: ${navigation.key.id}")
        Button(onClick = {
            navigation.close()
        }) {
            Text("Close")
        }
        Button(onClick = {
            navigation.complete()
        }) {
            Text("Complete")
        }
        Button(onClick = {
            navigation.completeFrom(DetailKey("->" + navigation.key.id))
        }) {
            Text("Complete from detail")
        }
        Button(onClick = {
            navigation.completeFrom(ResultKey())
        }) {
            Text("Complete from result")
        }
    }
}

@Serializable
class ResultKey : NavigationKey.WithResult<String>

class ExampleViewModel : ViewModel() {}

@NavigationDestination(ResultKey::class)
@Composable
fun ResultDestination() {
    val navigation = navigationHandle<ResultKey>()
    val cancelResult = registerForNavigationResult<String> {
        if (it == "Dismiss") return@registerForNavigationResult
        navigation.closeWithoutCallback()
    }
    navigation.configure {
        onCloseRequested {
            cancelAnd {
                cancelResult.open(DialogResultKey())
            }
        }
    }
    val vm = viewModel {
        ExampleViewModel()
    }

    TitledColumn("Results") {
        Text("${navigation.hashCode()}")
        Text("${vm.hashCode()}")
        Button(onClick = {
            navigation.complete("A")
        }) {
            Text("A")
        }
        Button(onClick = {
            navigation.complete("B")
        }) {
            Text("B")
        }

        Button(onClick = {
            navigation.complete("C")
        }) {
            Text("C")
        }

        Button(onClick = {
            navigation.completeFrom(ResultKey())
        }) {
            Text("Delegate")
        }
        Button(onClick = {
            navigation.completeFrom(DialogResultKey())
        }) {
            Text("Delegate Dialog")
        }
        Button(onClick = {
            navigation.close()
        }) {
            Text("Close")
        }
    }
}

@Serializable
data class SyntheticKey(val message: String) : NavigationKey

@NavigationDestination(SyntheticKey::class)
val syntheticDestination = syntheticDestination<SyntheticKey> {
    println("SyntheticKey ${key.message}")
}

@Serializable
class ScreenWithViewModelKey : NavigationKey.WithResult<String>

class ScreenWithViewModelViewModel : ViewModel() {
    private val navigation = navigationHandle<ScreenWithViewModelKey>()

    fun onComplete(result: String) {
        navigation.complete(result)
    }
}

@NavigationDestination(ScreenWithViewModelKey::class)
val screenWithViewModelDestination = navigationDestination<ScreenWithViewModelKey> {
    val localOwner = LocalViewModelStoreOwner.current
    val viewModel = viewModel<ScreenWithViewModelViewModel> {
        createEnroViewModel {
            ScreenWithViewModelViewModel()
        }
    }
    TitledColumn("Screen with ViewModel") {
        Button(onClick = { viewModel.onComplete("From ViewModel") }) {
            Text("Complete")
        }
    }
}

@Serializable
class DialogKey(
    val title: String = "Dialog",
) : NavigationKey

@NavigationDestination(DialogKey::class)
val dialogDestination = navigationDestination<DialogKey>(
    metadata = mapOf(
        DialogSceneStrategy.dialog(
            DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
            )
        )
    )
) {
    val navigation = navigationHandle<DialogKey>()
    TitledColumn(
        title = navigation.key.title,
        modifier = Modifier
            .shadow(2.dp)
    ) {
        Button(onClick = {
            navigation.complete()
        }) {
            Text("Complete")
        }
        Button(onClick = {
            navigation.close()
        }) {
            Text("Close")
        }
    }
}

@Serializable
class DirectDialogKey : NavigationKey

@OptIn(ExperimentalMaterial3Api::class)
@NavigationDestination(DirectDialogKey::class)
val directDialogDestination = navigationDestination<DirectDialogKey>(
    metadata = mapOf(DirectOverlaySceneStrategy.overlay()),
) {
    val navigation = navigationHandle<NavigationKey>()
    AlertDialog(
        onDismissRequest = { navigation.close() },
        shape = MaterialTheme.shapes.medium,
        containerColor = MaterialTheme.colors.background,
        title = {
            Text("Direct Dialog")
        },
        confirmButton = {
            Button(onClick = { navigation.complete() }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = { navigation.close() }) {
                Text("Dismiss")
            }
        },
        properties = DialogProperties(
            dismissOnClickOutside = false,
        )
    )
}

@Serializable
class DirectButtonKey : NavigationKey

@NavigationDestination(DirectButtonKey::class)
val directButtonDestination = navigationDestination<DirectButtonKey>(
    metadata = mapOf(DirectOverlaySceneStrategy.overlay()),
) {
    val navigation = navigationHandle<NavigationKey>()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.onBackground.copy(alpha = 0.16f)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Button(onClick = {
            navigation.complete()
        }) {
            Text("Complete")
        }
        Button(onClick = {
            navigation.close()
        }) {
            Text("Close")
        }
    }
}

@Serializable
class DirectBottomSheetKey : NavigationKey

@NavigationDestination(DirectBottomSheetKey::class)
@OptIn(ExperimentalMaterial3Api::class)
val directBottomSheetDestination = navigationDestination<DirectBottomSheetKey>(
    metadata = mapOf(DirectOverlaySceneStrategy.overlay()),
) {
    val navigation = navigationHandle<NavigationKey>()
    ModalBottomSheet(
        containerColor = MaterialTheme.colors.background,
        onDismissRequest = { navigation.close() },
    ) {
        TitledColumn(
            title = "Direct Bottom Sheet",
            modifier = Modifier,
        ) {
            Text(
                text = "This is some text in the bottom sheet to create some addtional content and space to make sure that " +
                        "the bottom sheet actually is long enough to have some interesting things in it and scroll, and " +
                        "other things like that. This text really does not have any meaning at all. " +
                        "This is some text in the bottom sheet to create some addtional content and space to make sure that " +
                        "the bottom sheet actually is long enough to have some interesting things in it and scroll, and " +
                        "other things like that. This text really does not have any meaning at all."
            )
            Text(
                text = "This is some text in the bottom sheet to create some addtional content and space to make sure that " +
                        "the bottom sheet actually is long enough to have some interesting things in it and scroll, and " +
                        "other things like that. This text really does not have any meaning at all. " +
                        "This is some text in the bottom sheet to create some addtional content and space to make sure that " +
                        "the bottom sheet actually is long enough to have some interesting things in it and scroll, and " +
                        "other things like that. This text really does not have any meaning at all."
            )
            Button(
                onClick = { navigation.complete() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Complete")
            }
            Button(
                onClick = { navigation.close() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Close")
            }
        }
    }
}


@Serializable
class EmptyKey : NavigationKey

@NavigationDestination(EmptyKey::class)
val emptyDestination = navigationDestination<EmptyKey> {}

@Serializable
class NestedKey : NavigationKey

@NavigationDestination(NestedKey::class)
val nestedDestination = navigationDestination<NestedKey> {
    val container = rememberNavigationContainer(
        backstack = listOf(EmptyKey().asInstance()),
    )
    TitledColumn(
        title = "Nested",
    ) {
        Button(
            onClick = {
                container.execute(
                    NavigationOperation.open(
                        ListKey().asInstance()
                    )
                )
            }
        ) {
            Text("Push List")
        }
        NavigationDisplay(container)
    }
}

@Serializable
class FlowKey : NavigationKey.WithResult<Pair<String, String>>

class FlowViewModel : ViewModel() {
    val navigation = navigationHandle<FlowKey>()

    val resultFlow by registerForFlowResult(
        navigationHandle = navigation,
        flow = {
            val firstResult = open { ResultKey() }
            val secondResult = open { ResultKey() }
            Pair(firstResult, secondResult)
        },
        onCompleted = { result ->
            navigation.complete(result)
        }
    )
}

@NavigationDestination(FlowKey::class)

val flowDestination = navigationDestination<FlowKey> {
    val viewModel = viewModel<FlowViewModel> {
        createEnroViewModel {
            FlowViewModel()
        }
    }
    val container = rememberNavigationContainerForFlow(viewModel.resultFlow)
    NavigationDisplay(container)
}

@Serializable
class DialogResultKey : NavigationKey.WithResult<String>

@NavigationDestination(DialogResultKey::class)
val dialogResultDestination = navigationDestination<DialogResultKey>(
    metadata = mapOf(
        DirectOverlaySceneStrategy.overlay()
    )
) {
    AlertDialog(
        onDismissRequest = { navigation.close() },
        shape = MaterialTheme.shapes.medium,
        containerColor = MaterialTheme.colors.background,
        title = {
            Text("Direct Dialog")
        },
        confirmButton = {
            Button(onClick = { navigation.complete("Confirm") }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = { navigation.complete("Dismiss") }) {
                Text("Dismiss")
            }
        },
        properties = DialogProperties(
            dismissOnClickOutside = false,
        )
    )
}
