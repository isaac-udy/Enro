package dev.enro.tests.application.compose.results

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.close
import dev.enro.core.closeWithResult
import dev.enro.core.compose.dialog.DialogDestination
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.result.flows.NavigationFlowReference
import dev.enro.core.result.flows.registerForFlowResult
import dev.enro.core.result.flows.rememberNavigationFlowReference
import dev.enro.core.result.flows.requireStep
import dev.enro.tests.application.compose.common.TitledColumn
import dev.enro.viewmodel.navigationHandle
import kotlinx.parcelize.Parcelize

@Parcelize
object ComposeManagedResultFlow : NavigationKey.SupportsPush {

    @Parcelize
    internal class FirstResult : NavigationKey.SupportsPush.WithResult<String>

    @Parcelize
    internal class PresentedResult : NavigationKey.SupportsPresent.WithResult<String>

    @Parcelize
    internal class SecondResult : NavigationKey.SupportsPush.WithResult<String>

    @Parcelize
    internal class TransientResult : NavigationKey.SupportsPush.WithResult<String>

    @Parcelize
    internal class ThirdResult : NavigationKey.SupportsPush.WithResult<String>

    @Parcelize
    internal class FinalScreen(
        val navigationFlowReference: NavigationFlowReference,
        val text: String,
    ) : NavigationKey.SupportsPush.WithResult<Unit>

}


class ComposeManagedResultViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val navigation by navigationHandle<ComposeManagedResultFlow>()

    val resultFlow by registerForFlowResult(
        savedStateHandle = savedStateHandle,
        flow = {
            val firstResult = push { ComposeManagedResultFlow.FirstResult() }
            val presentedResult = present { ComposeManagedResultFlow.PresentedResult() }
            val secondResult = push {
                default("default")
                ComposeManagedResultFlow.SecondResult()
            }
            val transientResult = push {
                transient()
                dependsOn(secondResult)
                ComposeManagedResultFlow.TransientResult()
            }
            val thirdResult = push { ComposeManagedResultFlow.ThirdResult() }

            push {
                ComposeManagedResultFlow.FinalScreen(
                    navigationFlowReference = navigationFlowReference,
                    text = """
                        First Result: $firstResult
                        Second Result: $secondResult
                        Transient Result: $transientResult
                        Third Result: $thirdResult
                        Presented Result: $presentedResult
                    """.trimIndent()
                )
            }
        },
        onCompleted = {
            navigation.close()
        }
    )
}

@NavigationDestination(ComposeManagedResultFlow::class)
@Composable
fun ComposeManagedResultFlowScreen(viewModel: ComposeManagedResultViewModel = viewModel()) {
    val container = rememberNavigationContainer(
        emptyBehavior = EmptyBehavior.CloseParent,
    )
    Box(modifier = Modifier.fillMaxSize()) {
        container.Render()
    }
}

@NavigationDestination(ComposeManagedResultFlow.FirstResult::class)
@Composable
fun FirstResultScreen() {
    val navigation = navigationHandle<ComposeManagedResultFlow.FirstResult>()
    TitledColumn(title = "First Result") {
        Button(onClick = { navigation.closeWithResult("A") }) {
            Text("Continue (A)")
        }

        Button(onClick = { navigation.closeWithResult("B") }) {
            Text("Continue (B)")
        }
    }
}

@NavigationDestination(ComposeManagedResultFlow.PresentedResult::class)
@Composable
fun PresentedResultScreen() = DialogDestination {
    val navigation = navigationHandle<ComposeManagedResultFlow.PresentedResult>()
    Dialog(onDismissRequest = { navigation.close() }) {
        Card {
            Column(
                Modifier
                    .background(MaterialTheme.colors.background)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Presented",
                    style = MaterialTheme.typography.h6
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navigation.closeWithResult("A") }) {
                    Text("Continue (A)")
                }

                Button(onClick = { navigation.closeWithResult("B") }) {
                    Text("Continue (B)")
                }
            }
        }
    }
}

@NavigationDestination(ComposeManagedResultFlow.SecondResult::class)
@Composable
fun SecondResultScreen() {
    val navigation = navigationHandle<ComposeManagedResultFlow.SecondResult>()
    TitledColumn(title = "Second Result") {
        Button(onClick = { navigation.closeWithResult("A") }) {
            Text("Continue (A)")
        }

        Button(onClick = { navigation.closeWithResult("B") }) {
            Text("Continue (B)")
        }
    }
}

@NavigationDestination(ComposeManagedResultFlow.TransientResult::class)
@Composable
fun SkipOnBackResultScreen() {
    val navigation = navigationHandle<ComposeManagedResultFlow.TransientResult>()
    TitledColumn(title = "Transient Result") {
        Text(text = "This screen will only be displayed if the result from the second screen has changed")

        Button(onClick = { navigation.closeWithResult("A") }) {
            Text("Continue (A)")
        }

        Button(onClick = { navigation.closeWithResult("B") }) {
            Text("Continue (B)")
        }
    }
}

@NavigationDestination(ComposeManagedResultFlow.ThirdResult::class)
@Composable
fun ThirdResultScreen() {
    val navigation = navigationHandle<ComposeManagedResultFlow.ThirdResult>()
    TitledColumn(title = "Third Result") {
        Button(onClick = { navigation.closeWithResult("A") }) {
            Text("Continue (A)")
        }

        Button(onClick = { navigation.closeWithResult("B") }) {
            Text("Continue (B)")
        }
    }
}

@NavigationDestination(ComposeManagedResultFlow.FinalScreen::class)
@Composable
fun FinalScreenScreen() {
    val navigation = navigationHandle<ComposeManagedResultFlow.FinalScreen>()
    val flowReference = rememberNavigationFlowReference(navigation.key.navigationFlowReference)
    TitledColumn(title = "Final Screen") {
        Text(text = navigation.key.text)

        Button(onClick = {
            flowReference.requireStep<ComposeManagedResultFlow.FirstResult>()
                .editStep()
        }) {
            Text("Edit First Result")
        }

        Button(onClick = {
            flowReference.requireStep<ComposeManagedResultFlow.SecondResult>()
                .editStep()
        }) {
            Text("Edit Second Result")
        }

        Button(onClick = {
            flowReference.requireStep<ComposeManagedResultFlow.ThirdResult>()
                .editStep()
        }) {
            Text("Edit Third Result")
        }

        Button(onClick = { navigation.closeWithResult(Unit) }) {
            Text("Finish")
        }
    }
}

