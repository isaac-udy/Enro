@file:OptIn(AdvancedEnroApi::class, ExperimentalEnroApi::class)

package dev.enro.tests.application.compose.results

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro.NavigationKey
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.annotations.NavigationDestination
import dev.enro.close
import dev.enro.complete
import dev.enro.navigationHandle
import dev.enro.result.flow.NavigationFlowReference
import dev.enro.result.flow.registerForFlowResult
import dev.enro.result.flow.rememberNavigationContainerForFlow
import dev.enro.result.flow.rememberNavigationFlowReference
import dev.enro.result.flow.requireStep
import dev.enro.tests.application.compose.common.TitledColumn
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.navigationDestination
import dev.enro.ui.scenes.directOverlay
import dev.enro.viewmodel.createEnroViewModel
import dev.enro.withMetadata
import kotlinx.serialization.Serializable

@Serializable
data object ComposeManagedResultFlow : NavigationKey {

    @Serializable
    internal class FirstResult : NavigationKey.WithResult<String>

    @Serializable
    internal class PresentedResult : NavigationKey.WithResult<String>

    @Serializable
    internal class SecondResult : NavigationKey.WithResult<String> {
        object MetadataKey : NavigationKey.MetadataKey<Int>(0)
    }

    @Serializable
    internal class TransientResult : NavigationKey.WithResult<String>

    @Serializable
    internal class ThirdResult : NavigationKey.WithResult<String>

    @Serializable
    internal class ConfirmThirdResultScreen(
        val result: String,
    ) : NavigationKey

    @Serializable
    internal class FinalScreen(
        val navigationFlowReference: NavigationFlowReference,
        val text: String,
    ) : NavigationKey.WithResult<Unit>
}

class ComposeManagedResultViewModel : ViewModel() {

    private val navigation by navigationHandle<ComposeManagedResultFlow>()

    val resultFlow by registerForFlowResult(
        flow = {
            val firstResult = open(ComposeManagedResultFlow.FirstResult())
            val presentedResult = open(ComposeManagedResultFlow.PresentedResult())

            // We're using extras here as a simple way to test that pushWithExtras/NavigationKey.withExtra work within
            // managed flows - this extra is verified by the associated tests, but has no real impact on the flow itself
            val secondResult = open(
                ComposeManagedResultFlow.SecondResult()
                    .withMetadata(
                        ComposeManagedResultFlow.SecondResult.MetadataKey,
                        ComposeManagedResultFlow.hashCode()
                    )
            )
            val transientResult = open(ComposeManagedResultFlow.TransientResult()) {
                transient()
                dependsOn(secondResult)
            }
            val thirdResult = open(ComposeManagedResultFlow.ThirdResult())

            open(ComposeManagedResultFlow.ConfirmThirdResultScreen(thirdResult)) {
                alwaysAfterPreviousStep()
            }

            open(
                ComposeManagedResultFlow.FinalScreen(
                    navigationFlowReference = navigationFlowReference,
                    text = """
                        First Result: $firstResult
                        Presented Result: $presentedResult
                        Second Result: $secondResult
                        Transient Result: $transientResult
                        Third Result: $thirdResult
                    """.trimIndent()
                )
            )
        },
        onCompleted = {
            navigation.close()
        }
    )
}

@NavigationDestination(ComposeManagedResultFlow::class)
@Composable
fun ComposeManagedResultFlowScreen(
    viewModel: ComposeManagedResultViewModel = viewModel {
        createEnroViewModel {
            ComposeManagedResultViewModel()
        }
    },
) {
    val container = rememberNavigationContainerForFlow(
        flow = viewModel.resultFlow,
    )
    Box(modifier = Modifier.fillMaxSize()) {
        NavigationDisplay(container)
    }
}

@NavigationDestination(ComposeManagedResultFlow.FirstResult::class)
@Composable
fun FirstResultScreen() {
    val navigation = navigationHandle<ComposeManagedResultFlow.FirstResult>()
    TitledColumn(title = "First Result") {
        Button(onClick = { navigation.complete("A") }) {
            Text("Continue (A)")
        }

        Button(onClick = { navigation.complete("B") }) {
            Text("Continue (B)")
        }
    }
}

@NavigationDestination(ComposeManagedResultFlow.PresentedResult::class)
internal val presentedResultScreen =
    navigationDestination<ComposeManagedResultFlow.PresentedResult>(
        metadata = { directOverlay() }
    ) {
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
                    Button(onClick = { navigation.complete("A") }) {
                        Text("Continue (A)")
                    }

                    Button(onClick = { navigation.complete("B") }) {
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
        Button(onClick = { navigation.complete("A") }) {
            Text("Continue (A)")
        }

        Button(onClick = { navigation.complete("B") }) {
            Text("Continue (B)")
        }

        Text(
            style = MaterialTheme.typography.caption,
            text = "Has extra: ${navigation.instance.metadata.get(ComposeManagedResultFlow.SecondResult.MetadataKey)}"
        )
    }
}

@NavigationDestination(ComposeManagedResultFlow.TransientResult::class)
@Composable
fun TransientResultScreen() {
    val navigation = navigationHandle<ComposeManagedResultFlow.TransientResult>()
    TitledColumn(title = "Transient Result") {
        Text(
            text = "This screen will only be displayed if the result from the second screen has changed"
        )

        Button(onClick = { navigation.complete("A") }) {
            Text("Continue (A)")
        }

        Button(onClick = { navigation.complete("B") }) {
            Text("Continue (B)")
        }
    }
}

@NavigationDestination(ComposeManagedResultFlow.ThirdResult::class)
@Composable
fun ThirdResultScreen() {
    val navigation = navigationHandle<ComposeManagedResultFlow.ThirdResult>()
    TitledColumn(title = "Third Result") {
        Button(onClick = { navigation.complete("A") }) {
            Text("Continue (A)")
        }

        Button(onClick = { navigation.complete("B") }) {
            Text("Continue (B)")
        }
    }
}

@NavigationDestination(ComposeManagedResultFlow.ConfirmThirdResultScreen::class)
internal val confirmThirdResultScreen =
    navigationDestination<ComposeManagedResultFlow.ConfirmThirdResultScreen>(
        metadata = { directOverlay() }
    ) {
        AlertDialog(
            title = { Text("Confirm Third Result") },
            text = {
                Text("You set the result to ${navigation.key.result}.\n\nDo you want to continue?")
            },
            onDismissRequest = { navigation.close() },
            confirmButton = {
                Button(onClick = { navigation.complete() }) {
                    Text("Continue")
                }
            },
            dismissButton = {
                Button(onClick = { navigation.close() }) {
                    Text("Close")
                }
            },
        )
    }

@NavigationDestination(ComposeManagedResultFlow.FinalScreen::class)
@Composable
fun FinalScreenScreen() {
    val navigation = navigationHandle<ComposeManagedResultFlow.FinalScreen>()
    val flowReference = rememberNavigationFlowReference(navigation.key.navigationFlowReference)
    val linesOfText = remember(navigation.key.text) {
        navigation.key.text.lines()
    }
    TitledColumn(title = "Final Screen") {
        linesOfText.forEach {
            Text(it)
        }

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

        Button(onClick = { navigation.complete(Unit) }) {
            Text("Finish")
        }
    }
}

