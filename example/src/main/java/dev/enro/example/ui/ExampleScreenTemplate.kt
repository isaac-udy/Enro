package dev.enro.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.compose.navigationHandle
import dev.enro.core.parentContainer
import dev.enro.example.*
import dev.enro.example.R
import dev.enro.example.data.sentenceId
import dev.enro.viewmodel.navigationHandle
import dev.enro.viewmodel.withNavigationHandle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


class ExampleScreenViewModel : ViewModel() {

    private val mutableTicks: MutableStateFlow<Int> = MutableStateFlow(0)
    val ticks: StateFlow<Int> = mutableTicks

    private val navigation by navigationHandle<NavigationKey> { }

    init {
        viewModelScope.launch {
            while (isActive) {
                if (navigation.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    mutableTicks.value = mutableTicks.value + 1
                }
                delay(1000)
            }
        }
    }
}

@Composable
fun ExampleScreenTemplate(
    title: String,
    modifier: Modifier = Modifier.fillMaxSize(),
    buttons: List<Pair<String, AnyOpenInstruction>> = defaultNavigationButtons(),
    overflow: List<Pair<String, NavigationInstruction?>> = defaultNavigationOverflow(),
) {
    val scrollState = rememberScrollState()
    val viewModel = viewModel<ExampleScreenViewModel>(factory = ViewModelProvider.NewInstanceFactory().withNavigationHandle())
    val navigation = navigationHandle()
    val backstack by parentContainer?.backstackFlow?.collectAsState() ?: mutableStateOf(null)
    var backstackItems by remember { mutableStateOf(listOf<String>()) }
    navigation.instruction.additionalData.putString("example", navigation.sentenceId)

    DisposableEffect(backstack) {
        backstackItems = backstack
            .orEmpty()
            .takeWhile { it.instructionId != navigation.id }
            .map { instruction ->
                instruction.sentenceId
            }
            .reversed()
        onDispose { }
    }

    Surface(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 8.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.h4,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    val ticks by viewModel.ticks.collectAsState()
                    Text(text = ticks.toString())
                }
                Text(
                    text = stringResource(R.string.example_content),
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = "Current Destination:",
                    modifier = Modifier.padding(top = 24.dp),
                    style = MaterialTheme.typography.h6
                )
                Text(
                    text = navigation.sentenceId,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Text(
                    text = "Backstack:",
                    modifier = Modifier.padding(top = 24.dp),
                    style = MaterialTheme.typography.h6
                )
                Column(
                    modifier = Modifier.padding(start = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    backstackItems.forEach {
                        key(it) {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.caption
                            )
                        }
                    }
                }
            }

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Column(
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .padding(top = 16.dp)
            ) {
                buttons.forEach { button ->
                    OutlinedButton(
                        modifier = Modifier.padding(top = 6.dp, bottom = 6.dp),
                        onClick = {
                            navigation.executeInstruction(button.second)
                        }) {
                        Text(button.first)
                    }
                }

                val selectNavigationInstructionState = rememberSelectNavigationInstructionState()
                OutlinedButton(
                    modifier = Modifier.padding(top = 6.dp, bottom = 6.dp),
                    onClick = {
                        selectNavigationInstructionState.show(overflow)
                    }) {
                    Text("Other")
                }
            }
        }
    }
}

private fun defaultNavigationButtons(): List<Pair<String, AnyOpenInstruction>> = listOf(
    "Push (Compose)" to NavigationInstruction.Push(ExampleComposableKey()),
    "Push (Fragment)" to NavigationInstruction.Push(ExampleFragmentKey()),
)

private fun defaultNavigationOverflow(): List<Pair<String, NavigationInstruction?>> = listOf(
    "Compose" to null,
    "Present (Compose)" to NavigationInstruction.Present(ExampleComposableKey()),
    "Present Dialog (Compose)" to NavigationInstruction.Present(ExampleDialogComposableKey()),
    "Present Bottom Sheet (Compose)" to NavigationInstruction.Present(ExampleComposableBottomSheetKey()),
    "Replace Root (Compose)" to NavigationInstruction.ReplaceRoot(ExampleComposableKey()),
    "Fragment" to null,
    "Present (Fragment)" to NavigationInstruction.Present(ExampleFragmentKey()),
    "Present Dialog (Fragment)" to NavigationInstruction.Present(ExampleDialogFragmentKey()),
    "Replace Root (Fragment)" to NavigationInstruction.ReplaceRoot(ExampleFragmentKey()),
    "" to null,
    "Close" to NavigationInstruction.Close,
    "Request Close" to NavigationInstruction.RequestClose,
)