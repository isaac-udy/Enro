package dev.enro.tests.application.savedstate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.savedstate.SavedState
import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.tests.application.activity.applyInsetsForContentView
import dev.enro.tests.application.compose.common.TitledColumn
import dev.enro.ui.EmptyBehavior
import dev.enro.ui.NavigationContainerState
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

@Serializable
object SavedStateDestination : NavigationKey {

    @Serializable
    class TextInput : NavigationKey

    @Serializable
    class TextInputFromViewModel : NavigationKey

}

@NavigationDestination(SavedStateDestination::class)
class SavedStateActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            TitledColumn(title = "Saved State") {
                val isPopupVisible = remember { mutableStateOf(false) }
                val saved = rememberSaveable { mutableStateOf<SavedState?>(null) }
                val container = rememberNavigationContainer(
                    backstack = emptyList(),
                    emptyBehavior = EmptyBehavior.allowEmpty(),
                )
                Button(onClick = {
                    isPopupVisible.value = true
                }) {
                    Text(text = "Open Destination")
                }
                Button(onClick = {
                    saved.value = container.saveState()
                }) {
                    Text(text = "Save")
                }
                Button(onClick = {
                    if (saved.value != null) {
                        container.restoreState(saved.value!!)
                    }
                }) {
                    Text(text = "Restore")
                }
                NavigationDisplay(
                    state = container,
                    modifier = Modifier.fillMaxSize(),
                )

                SelectDestinationPopup(
                    isPopupVisible = isPopupVisible.value,
                    onDismissRequest = { isPopupVisible.value = false },
                    container = container,
                    destinations = listOf(
                        SavedStateDestination.TextInput(),
                        SavedStateDestination.TextInputFromViewModel(),
                    )
                )
            }
        }
        applyInsetsForContentView()
    }
}

@Composable
private fun SelectDestinationPopup(
    isPopupVisible: Boolean,
    onDismissRequest: () -> Unit,
    container: NavigationContainerState,
    destinations: List<NavigationKey>,
) {
    if (isPopupVisible) {
        Popup(
            onDismissRequest = { onDismissRequest() }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Select Destination",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    destinations.forEach { item ->
                        TextButton(
                            onClick = {
                                onDismissRequest()
                                container.execute(
                                    NavigationOperation.Open(item.asInstance()),
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = remember { item::class.simpleName } ?: "Unknown",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@NavigationDestination(SavedStateDestination.TextInput::class)
fun TextInputScreen() {
    val text = rememberSaveable { mutableStateOf("") }
    TitledColumn(title = "Text Input") {
        TextField(
            value = text.value,
            onValueChange = { text.value = it },
            label = { Text("Enter text") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

internal class TextInputViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val text: StateFlow<String> = savedStateHandle.getStateFlow("text", "")

    fun setText(value: String) {
        savedStateHandle["text"] = value
    }
}

@Composable
@NavigationDestination(SavedStateDestination.TextInputFromViewModel::class)
fun TextInputFromViewModelScreen() {
    val viewModel = viewModel<TextInputViewModel>()
    val text by viewModel.text.collectAsState()
    TitledColumn(title = "Text Input from ViewModel") {
        TextField(
            value = text,
            onValueChange = { viewModel.setText(it) },
            label = { Text("Enter text") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
