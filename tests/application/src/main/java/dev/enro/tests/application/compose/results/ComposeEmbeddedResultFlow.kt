package dev.enro.tests.application.compose.results

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.closeWithResult
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.registerForNavigationResult
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.accept
import dev.enro.core.navigationHandle
import dev.enro.core.result.deliverResultFromPresent
import dev.enro.core.result.deliverResultFromPush
import dev.enro.tests.application.compose.common.TitledColumn
import kotlinx.parcelize.Parcelize

@Parcelize
object ComposeEmbeddedResultFlow : NavigationKey.SupportsPush {
    @Parcelize
    internal object Root : NavigationKey.SupportsPush

    @Parcelize
    internal data class InsideContainer(
        val currentResult: String,
    ) : NavigationKey.SupportsPush.WithResult<String>

    @Parcelize
    internal data class OutsideContainer(
        val currentResult: String,
    ) : NavigationKey.SupportsPush.WithResult<String>

    @Parcelize
    internal data class Activity(
        val currentResult: String,
    ) : NavigationKey.SupportsPresent.WithResult<String>
}

@NavigationDestination(ComposeEmbeddedResultFlow::class)
@Composable
fun ComposeEmbeddedResultFlowScreen() {
    val container = rememberNavigationContainer(
        root = ComposeEmbeddedResultFlow.Root,
        emptyBehavior = EmptyBehavior.CloseParent,
        filter = accept {
            key<ComposeEmbeddedResultFlow.Root>()
            key<ComposeEmbeddedResultFlow.InsideContainer>()
        }
    )
    Box(modifier = Modifier.fillMaxSize()) {
        container.Render()
    }
}

@NavigationDestination(ComposeEmbeddedResultFlow.Root::class)
@Composable
fun ComposeEmbeddedResultFlowRoot() {
    var lastResult by rememberSaveable {
        mutableStateOf("(none)")
    }
    val resultChannel = registerForNavigationResult<String> {
        lastResult = it
    }

    TitledColumn(title = "Embedded Result Flow") {
        Text("Last Result: $lastResult")

        Button(onClick = {
            resultChannel.push(ComposeEmbeddedResultFlow.InsideContainer("in"))
        }) {
            Text("Navigate Inside Container")
        }

        Button(onClick = {
            resultChannel.push(ComposeEmbeddedResultFlow.OutsideContainer("out"))
        }) {
            Text("Navigate Outside Container")
        }

        Button(onClick = {
            resultChannel.present(ComposeEmbeddedResultFlow.Activity("act"))
        }) {
            Text("Navigate Activity")
        }
    }
}

@NavigationDestination(ComposeEmbeddedResultFlow.InsideContainer::class)
@Composable
fun ComposeEmbeddedResultFlowInsideContainer() {
    val navigation = navigationHandle<ComposeEmbeddedResultFlow.InsideContainer>()

    TitledColumn(title = "Embedded Result Flow Inside Container") {
        Button(onClick = {
            navigation.deliverResultFromPush(
                ComposeEmbeddedResultFlow.InsideContainer(navigation.key.currentResult + "-> in a")
            )
        }) {
            Text("Navigate Inside Container (a)")
        }
        Button(onClick = {
            navigation.deliverResultFromPush(
                ComposeEmbeddedResultFlow.InsideContainer(navigation.key.currentResult + "-> in b")
            )
        }) {
            Text("Navigate Inside Container (b)")
        }

        Button(onClick = {
            navigation.deliverResultFromPush(
                ComposeEmbeddedResultFlow.OutsideContainer(navigation.key.currentResult + "-> out 1")
            )
        }) {
            Text("Navigate Outside Container (1)")
        }

        Button(onClick = {
            navigation.deliverResultFromPush(
                ComposeEmbeddedResultFlow.OutsideContainer(navigation.key.currentResult + "-> out 2")
            )
        }) {
            Text("Navigate Outside Container (2)")
        }

        Button(onClick = {
            navigation.deliverResultFromPresent(
                ComposeEmbeddedResultFlow.Activity(navigation.key.currentResult + "-> act x")
            )
        }) {
            Text("Navigate Activity (x)")
        }

        Button(onClick = {
            navigation.deliverResultFromPresent(
                ComposeEmbeddedResultFlow.Activity(navigation.key.currentResult + "-> act y")
            )
        }) {
            Text("Navigate Activity (y)")
        }

        Button(onClick = {
            navigation.closeWithResult(navigation.key.currentResult)
        }) {
            Text("Finish")
        }
    }
}

@NavigationDestination(ComposeEmbeddedResultFlow.OutsideContainer::class)
@Composable
fun ComposeEmbeddedResultFlowOutsideContainerContainer() {
    val navigation = navigationHandle<ComposeEmbeddedResultFlow.OutsideContainer>()

    TitledColumn(title = "Embedded Result Flow Outside Container") {
        Button(onClick = {
            navigation.deliverResultFromPush(
                ComposeEmbeddedResultFlow.OutsideContainer(navigation.key.currentResult + "-> out 1")
            )
        }) {
            Text("Navigate Outside Container (1)")
        }

        Button(onClick = {
            navigation.deliverResultFromPush(
                ComposeEmbeddedResultFlow.OutsideContainer(navigation.key.currentResult + "-> out 2")
            )
        }) {
            Text("Navigate Outside Container (2)")
        }

        Button(onClick = {
            navigation.deliverResultFromPresent(
                ComposeEmbeddedResultFlow.Activity(navigation.key.currentResult + "-> act x")
            )
        }) {
            Text("Navigate Activity (x)")
        }

        Button(onClick = {
            navigation.deliverResultFromPresent(
                ComposeEmbeddedResultFlow.Activity(navigation.key.currentResult + "-> act y")
            )
        }) {
            Text("Navigate Activity (y)")
        }

        Button(onClick = {
            navigation.closeWithResult(navigation.key.currentResult)
        }) {
            Text("Finish")
        }
    }
}

@NavigationDestination(ComposeEmbeddedResultFlow.Activity::class)
class ComposeEmbeddedResultFlowActivity : AppCompatActivity() {

    private val navigation by navigationHandle<ComposeEmbeddedResultFlow.Activity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Content()
            }
        }
    }

    @Composable
    private fun Content() {
        TitledColumn(title = "Embedded Result Flow Activity") {
            Button(onClick = {
                navigation.deliverResultFromPresent(
                    ComposeEmbeddedResultFlow.Activity(navigation.key.currentResult + "-> act x")
                )
            }) {
                Text("Navigate Activity (x)")
            }

            Button(onClick = {
                navigation.deliverResultFromPresent(
                    ComposeEmbeddedResultFlow.Activity(navigation.key.currentResult + "-> act y")
                )
            }) {
                Text("Navigate Activity (y)")
            }

            Button(onClick = {
                navigation.closeWithResult(navigation.key.currentResult)
            }) {
                Text("Finish")
            }
        }
    }
}