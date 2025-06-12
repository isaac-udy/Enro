package dev.enro.tests.application.compose.results

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.enro.NavigationKey
import dev.enro.accept
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.complete
import dev.enro.completeFrom
import dev.enro.navigationHandle
import dev.enro.result.open
import dev.enro.result.registerForNavigationResult
import dev.enro.tests.application.compose.common.TitledColumn
import dev.enro.ui.EmptyBehavior
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

@Serializable
object ComposeEmbeddedResultFlow : NavigationKey {
    @Serializable
    internal object Root : NavigationKey

    @Serializable
    internal data class InsideContainer(
        val currentResult: String,
    ) : NavigationKey.WithResult<String>

    @Serializable
    internal data class OutsideContainer(
        val currentResult: String,
    ) : NavigationKey.WithResult<String>

    @Serializable
    internal data class Activity(
        val currentResult: String,
    ) : NavigationKey.WithResult<String>
}

@NavigationDestination(ComposeEmbeddedResultFlow::class)
@Composable
fun ComposeEmbeddedResultFlowScreen() {
    val container = rememberNavigationContainer(
        backstack = listOf(ComposeEmbeddedResultFlow.Root.asInstance()),
        emptyBehavior = EmptyBehavior.closeParent(),
        filter = accept {
            key<ComposeEmbeddedResultFlow.Root>()
            key<ComposeEmbeddedResultFlow.InsideContainer>()
        }
    )
    NavigationDisplay(
        state = container,
        modifier = Modifier.fillMaxSize(),
    )
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
            resultChannel.open(ComposeEmbeddedResultFlow.InsideContainer("in"))
        }) {
            Text("Navigate Inside Container")
        }

        Button(onClick = {
            resultChannel.open(ComposeEmbeddedResultFlow.OutsideContainer("out"))
        }) {
            Text("Navigate Outside Container")
        }

        Button(onClick = {
            resultChannel.open(ComposeEmbeddedResultFlow.Activity("act"))
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
            navigation.completeFrom(
                ComposeEmbeddedResultFlow.InsideContainer(navigation.key.currentResult + "-> in a")
            )
        }) {
            Text("Navigate Inside Container (a)")
        }
        Button(onClick = {
            navigation.completeFrom(
                ComposeEmbeddedResultFlow.InsideContainer(navigation.key.currentResult + "-> in b")
            )
        }) {
            Text("Navigate Inside Container (b)")
        }

        Button(onClick = {
            navigation.completeFrom(
                ComposeEmbeddedResultFlow.OutsideContainer(navigation.key.currentResult + "-> out 1")
            )
        }) {
            Text("Navigate Outside Container (1)")
        }

        Button(onClick = {
            navigation.completeFrom(
                ComposeEmbeddedResultFlow.OutsideContainer(navigation.key.currentResult + "-> out 2")
            )
        }) {
            Text("Navigate Outside Container (2)")
        }

        Button(onClick = {
            navigation.completeFrom(
                ComposeEmbeddedResultFlow.Activity(navigation.key.currentResult + "-> act x")
            )
        }) {
            Text("Navigate Activity (x)")
        }

        Button(onClick = {
            navigation.completeFrom(
                ComposeEmbeddedResultFlow.Activity(navigation.key.currentResult + "-> act y")
            )
        }) {
            Text("Navigate Activity (y)")
        }

        Button(onClick = {
            navigation.complete(navigation.key.currentResult)
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
            navigation.completeFrom(
                ComposeEmbeddedResultFlow.OutsideContainer(navigation.key.currentResult + "-> out 1")
            )
        }) {
            Text("Navigate Outside Container (1)")
        }

        Button(onClick = {
            navigation.completeFrom(
                ComposeEmbeddedResultFlow.OutsideContainer(navigation.key.currentResult + "-> out 2")
            )
        }) {
            Text("Navigate Outside Container (2)")
        }

        Button(onClick = {
            navigation.completeFrom(
                ComposeEmbeddedResultFlow.Activity(navigation.key.currentResult + "-> act x")
            )
        }) {
            Text("Navigate Activity (x)")
        }

        Button(onClick = {
            navigation.completeFrom(
                ComposeEmbeddedResultFlow.Activity(navigation.key.currentResult + "-> act y")
            )
        }) {
            Text("Navigate Activity (y)")
        }

        Button(onClick = {
            navigation.complete(navigation.key.currentResult)
        }) {
            Text("Finish")
        }
    }
}