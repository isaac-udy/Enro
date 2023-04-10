package dev.enro.example.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import dev.enro.core.container.emptyBackstack
import dev.enro.core.parentContainer
import dev.enro.example.R
import dev.enro.example.core.data.sentenceId
import dev.enro.example.destinations.compose.BottomSheetComposable
import dev.enro.example.destinations.compose.DialogComposable
import dev.enro.example.destinations.compose.ExampleComposable
import dev.enro.example.destinations.fragment.DialogFragmentKey
import dev.enro.example.destinations.fragment.ExampleFragment
import dev.enro.example.destinations.restoration.SaveRootState
import dev.enro.viewmodel.navigationHandle
import dev.enro.viewmodel.withNavigationHandle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID


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
    val backstack = parentContainer?.backstack ?: emptyBackstack()
    var backstackItems by remember { mutableStateOf(listOf<String>()) }
    navigation.instruction.extras["example"] = navigation.sentenceId

    val ticks by viewModel.ticks.collectAsState()
    val savedState = rememberSaveable { UUID.randomUUID().toString() }

    DisposableEffect(backstack) {
        backstackItems = backstack
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
                Text(
                    text = title,
                    style = MaterialTheme.typography.h4,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Text(text = "${savedState.take(8)}@$ticks")

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
    "Push (Compose)" to NavigationInstruction.Push(ExampleComposable()),
    "Push (Fragment)" to NavigationInstruction.Push(ExampleFragment()),
)

private fun defaultNavigationOverflow(): List<Pair<String, NavigationInstruction?>> = listOf(
    "Compose" to null,
    "Present (Compose)" to NavigationInstruction.Present(ExampleComposable()),
    "Present Dialog (Compose)" to NavigationInstruction.Present(DialogComposable()),
    "Present Bottom Sheet (Compose)" to NavigationInstruction.Present(BottomSheetComposable()),
    "Replace Root (Compose)" to NavigationInstruction.ReplaceRoot(ExampleComposable()),
    "Fragment" to null,
    "Present (Fragment)" to NavigationInstruction.Present(ExampleFragment()),
    "Present Dialog (Fragment)" to NavigationInstruction.Present(DialogFragmentKey()),
    "Replace Root (Fragment)" to NavigationInstruction.ReplaceRoot(ExampleFragment()),
    "" to null,
    "Save/Restore State" to NavigationInstruction.Present(SaveRootState()),
    "Close" to NavigationInstruction.Close,
    "Request Close" to NavigationInstruction.RequestClose,
)