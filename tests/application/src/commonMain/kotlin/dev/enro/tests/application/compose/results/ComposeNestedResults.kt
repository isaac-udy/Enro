package dev.enro.tests.application.compose.results

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import dev.enro.NavigationKey
import dev.enro.accept
import dev.enro.acceptNone
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.complete
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.result.open
import dev.enro.result.registerForNavigationResult
import dev.enro.tests.application.compose.common.TitledColumn
import dev.enro.tests.application.compose.common.TitledRow
import dev.enro.ui.EmptyBehavior
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

@Serializable
object ComposeNestedResults : NavigationKey {
    @Serializable
    internal object Receiver : NavigationKey

    @Serializable
    internal object NestedSenderContainer : NavigationKey

    @Serializable
    internal object Sender : NavigationKey.WithResult<String>
}

@NavigationDestination(ComposeNestedResults::class)
@Composable
fun ComposeNestedResults() {
    val primary = rememberNavigationContainer(
        backstack = backstackOf(ComposeNestedResults.Receiver.asInstance()),
        filter = acceptNone(),
    )
    val secondary = rememberNavigationContainer(
        backstack = backstackOf(),
        emptyBehavior = EmptyBehavior.allowEmpty {
            primary.context.requestActive()
        },
        filter = accept {
            key(ComposeNestedResults.NestedSenderContainer)
        }
    )
    val isLandscape = LocalWindowInfo.current.let { windowInfo ->
        windowInfo.containerSize.width > windowInfo.containerSize.height
    }
    if (isLandscape) {
        TitledRow(title = "Compose Nested Results") {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                NavigationDisplay(
                    state = primary,
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                )
            }
            Box(
                modifier = Modifier.weight(1f)
            ) {
                NavigationDisplay(
                    state = secondary,
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                )
            }
        }
    } else {
        TitledColumn(
            title = "Compose Nested Results",
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .testTag("ComposeNestedResultsColumn")
        ) {
            NavigationDisplay(
                state = primary,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            NavigationDisplay(
                state = secondary,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        }
    }
}

@NavigationDestination(ComposeNestedResults.Receiver::class)
@Composable
fun ComposeNestedResultsReceiver() {
    var result by rememberSaveable { mutableStateOf("(None)") }
    val navigation = navigationHandle()
    val resultChannel = registerForNavigationResult<String>(
        onClosed = {
            result = "Closed"
        },
        onCompleted = {
            result = it
        }
    )
    TitledColumn(title = "Receiver") {
        Text(text = "Current Result: $result")
        val captionSize = MaterialTheme.typography.caption.fontSize * 0.8f
        Text(
            text = "You may need to scroll this container to see the buttons which perform actions." +
                    "\n\n" +
                    "If the secondary container has been opened, when using 'getResult', the result destination will be " +
                    "opened as a nested child of the secondary container. If this is the case, when the 'getResult' " +
                    "destination closes (whether it returns a result or not) it will cause the secondary container's " +
                    "destination to become empty, causing that container to trigger a close parent, which will cause " +
                    "that parent container to become empty, triggering the empty behavior defined in ComposeNestedResults; " +
                    "this means both the result sender destination and the secondary container destination will be closed, " +
                    "and this destination will become active again." +
                    "\n\n" +
                    "If the secondary container has not been opened, 'getResult' will push into the parent container of " +
                    "the ComposeNestedResults destination, and appear as a full screen destination.",
            style = MaterialTheme.typography.caption.copy(
                lineHeight = captionSize,
                fontSize = captionSize,
            ),
        )
        Button(onClick = { navigation.open(ComposeNestedResults.NestedSenderContainer) }) {
            Text(text = "Open Nested Sender Container")
        }
        Button(onClick = { resultChannel.open(ComposeNestedResults.Sender) }) {
            Text(text = "Get Result")
        }
    }
}

@NavigationDestination(ComposeNestedResults.NestedSenderContainer::class)
@Composable
fun ComposeNestedResultsNestedSenderContainer() {
    val container = rememberNavigationContainer(
        backstack = backstackOf(),
        emptyBehavior = EmptyBehavior.closeParent(),
        filter = accept {
            key(ComposeNestedResults.Sender)
        },
    )
    TitledColumn(title = "Nested Sender Container") {
        Box(modifier = Modifier.fillMaxSize()) {
            NavigationDisplay(container)
        }
    }
}

@NavigationDestination(ComposeNestedResults.Sender::class)
@Composable
fun ComposeNestedResultsSender() {
    val navigation = navigationHandle<ComposeNestedResults.Sender>()
    TitledColumn(title = "Sender") {
        Button(onClick = { navigation.complete("A") }) {
            Text(text = "Send A")
        }
        Button(onClick = { navigation.complete("B") }) {
            Text(text = "Send B")
        }
        Button(onClick = { navigation.close() }) {
            Text(text = "Close")
        }
    }
}
