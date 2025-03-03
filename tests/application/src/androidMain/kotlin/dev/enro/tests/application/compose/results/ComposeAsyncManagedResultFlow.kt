@file:OptIn(AdvancedEnroApi::class, ExperimentalEnroApi::class)

package dev.enro.tests.application.compose.results

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.close
import dev.enro.core.closeWithResult
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.result.flows.registerForFlowResult
import dev.enro.core.withExtra
import dev.enro.tests.application.compose.common.TitledColumn
import dev.enro.viewmodel.navigationHandle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.parcelize.Parcelize
import kotlin.random.Random

@Parcelize
object ComposeAsyncManagedResultFlow : NavigationKey.SupportsPush {

    @Parcelize
    internal class StepResult(
        val name: String,
    ) : NavigationKey.SupportsPush.WithResult<String>

    @Parcelize
    internal class FinalScreen(
        val data: String,
    ) : NavigationKey.SupportsPush.WithResult<Unit>

}

sealed interface AsyncData<T> {
    class Loading<T> : AsyncData<T>
    data class Loaded<T>(val data: T) : AsyncData<T>
}

data class ComposeAsyncFlowState(
    val initialData: AsyncData<String>?,
    val dataAfterStepOne: AsyncData<String>?,
    val dataAfterStepTwo: AsyncData<String>?,
)

class ComposeAsyncManagedResultViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val navigation by navigationHandle<ComposeAsyncManagedResultFlow>()

    val state = MutableStateFlow(
        ComposeAsyncFlowState(
            initialData = null,
            dataAfterStepOne = null,
            dataAfterStepTwo = null,
        )
    )

    val resultFlow by registerForFlowResult(
        savedStateHandle = savedStateHandle,
        flow = {
            val initialData = async {
                state.update { it.copy(initialData = AsyncData.Loading()) }
                val data = loadSuspendingData("init")
                state.update { it.copy(initialData = AsyncData.Loaded(data)) }
                return@async data
            }
            val firstStep = push { ComposeAsyncManagedResultFlow.StepResult("One") }
            val firstStepAsync = async(firstStep) {
                state.update { it.copy(dataAfterStepOne = AsyncData.Loading()) }
                val data = loadSuspendingData(firstStep)
                state.update { it.copy(dataAfterStepOne = AsyncData.Loaded(data)) }
                return@async data
            }

            val secondStep = pushWithExtras {
                // We're using extras here as a simple way to test that pushWithExtras/NavigationKey.withExtra work within
                // managed flows - this extra is verified by the associated tests, but has no real impact on the flow itself
                ComposeAsyncManagedResultFlow.StepResult("Two")
                    .withExtra("flowResultExtra", ComposeAsyncManagedResultFlow.hashCode())
            }
            val secondStepAsync = async(firstStep, secondStep) {
                state.update { it.copy(dataAfterStepTwo = AsyncData.Loading()) }
                val data = loadSuspendingData(secondStep)
                state.update { it.copy(dataAfterStepTwo = AsyncData.Loaded(data)) }
                return@async data
            }

            push {
                ComposeAsyncManagedResultFlow.FinalScreen(
                    data = """
                        Initial Data: $initialData
                        First Step: $firstStep
                            After: $firstStepAsync
                        Second Step: $secondStep
                            After: $secondStepAsync
                    """.trimIndent()
                )
            }
        },
        onCompleted = {
            navigation.close()
        }
    )

    private suspend fun loadSuspendingData(name: String): String {
        val delay = Random.nextLong(500, 2500)
        delay(delay)
        return "$name($delay)"
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@NavigationDestination(ComposeAsyncManagedResultFlow::class)
@Composable
fun ComposeAsyncManagedResultFlowScreen(viewModel: ComposeAsyncManagedResultViewModel = viewModel()) {
    val container = rememberNavigationContainer(
        emptyBehavior = EmptyBehavior.CloseParent,
    )
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colors.background)) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.height(8.dp))
            Text(
                text = "Managed Flow With Async Steps",
                style = MaterialTheme.typography.h6
            )
            Text(
                text = "Initial Data: " + when (state.initialData) {
                    null -> "Not Loaded"
                    is AsyncData.Loading -> "Loading..."
                    is AsyncData.Loaded -> (state.initialData as AsyncData.Loaded).data
                },
            )
            Text(
                text = "After First Step: " + when (state.dataAfterStepOne) {
                    null -> "Not Loaded"
                    is AsyncData.Loading -> "Loading..."
                    is AsyncData.Loaded -> (state.dataAfterStepOne as AsyncData.Loaded).data
                },
            )
            Text(
                text = "After Second Step: " + when (state.dataAfterStepTwo) {
                    null -> "Not Loaded"
                    is AsyncData.Loading -> "Loading..."
                    is AsyncData.Loaded -> (state.dataAfterStepTwo as AsyncData.Loaded).data
                },
            )
            Box(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .border(width = 1.dp, color = Color.Black.copy(alpha = ContentAlpha.disabled))
                    .clipToBounds()
            ) {
                container.Render()
            }
        }

        val isLoading = state.initialData is AsyncData.Loading ||
                state.dataAfterStepOne is AsyncData.Loading ||
                state.dataAfterStepTwo is AsyncData.Loading
        Crossfade(
            targetState = isLoading,
            modifier = Modifier.fillMaxSize()
        ) { loadingState ->
            if (!loadingState) return@Crossfade
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = ContentAlpha.disabled))
                    .pointerInteropFilter { true },
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@NavigationDestination(ComposeAsyncManagedResultFlow.StepResult::class)
@Composable
fun ComposeAsyncManagedResultFlowStepResultScreen() {
    val navigation = navigationHandle<ComposeAsyncManagedResultFlow.StepResult>()
    TitledColumn(title = "Step: ${navigation.key.name}") {

        Button(onClick = { navigation.closeWithResult("A") }) {
            Text("Continue (A)")
        }

        Button(onClick = { navigation.closeWithResult("B") }) {
            Text("Continue (B)")
        }

        val extra = navigation.instruction.extras["flowResultExtra"]
        if (extra != null) {
            Text(
                text = "Extra: ${navigation.instruction.extras["flowResultExtra"]}",
                style = MaterialTheme.typography.caption
            )
        }
    }
}

@NavigationDestination(ComposeAsyncManagedResultFlow.FinalScreen::class)
@Composable
fun ComposeAsyncManagedResultFlowFinalScreenScreen() {
    val navigation = navigationHandle<ComposeAsyncManagedResultFlow.FinalScreen>()
    TitledColumn(title = "Final Screen") {
        Text("Data: ${navigation.key.data}")

        Button(onClick = { navigation.closeWithResult(Unit) }) {
            Text("Finish")
        }
    }
}

