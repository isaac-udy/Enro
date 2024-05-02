package dev.enro.tests.application.compose.results

import android.content.res.Configuration
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.closeWithResult
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.registerForNavigationResult
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.accept
import dev.enro.core.container.acceptNone
import dev.enro.core.push
import dev.enro.core.requestClose
import dev.enro.tests.application.compose.common.TitledColumn
import dev.enro.tests.application.compose.common.TitledRow
import kotlinx.parcelize.Parcelize

@Parcelize
object ComposeNestedResults : NavigationKey.SupportsPush {
    @Parcelize
    internal object Receiver : NavigationKey.SupportsPush

    @Parcelize
    internal object NestedSenderContainer : NavigationKey.SupportsPush

    @Parcelize
    internal object Sender : NavigationKey.SupportsPush.WithResult<String>
}

@NavigationDestination(ComposeNestedResults::class)
@Composable
fun ComposeNestedResults() {
    val primary = rememberNavigationContainer(
        root = ComposeNestedResults.Receiver,
        emptyBehavior = EmptyBehavior.CloseParent,
        filter = acceptNone(),
    )
    val secondary = rememberNavigationContainer(
        emptyBehavior = EmptyBehavior.Action {
            primary.setActive()
            false
        },
        filter = accept {
            key(ComposeNestedResults.NestedSenderContainer)
        }
    )
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (isLandscape) {
        TitledRow(title = "Compose Nested Results") {
            Box(modifier = Modifier
                .weight(1f)
                .fillMaxHeight()) {
                primary.Render()
            }
            Box(modifier = Modifier
                .weight(1f)
                .fillMaxHeight()) {
                secondary.Render()
            }
        }
    }
    else {
        TitledColumn(title = "Compose Nested Results") {
            Box(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()) {
                primary.Render()
            }
            Box(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()) {
                secondary.Render()
            }
        }
    }
}

@NavigationDestination(ComposeNestedResults.Receiver::class)
@Composable
fun ComposeNestedResultsReceiver() {
    var result by remember { mutableStateOf("(None)") }
    val navigation = navigationHandle()
    val resultChannel = registerForNavigationResult<String>(
        onClosed = {
            result = "Closed"
        },
        onResult = {
            result = it
        }
    )
    TitledColumn(title = "Receiver", modifier = Modifier.verticalScroll(rememberScrollState())) {
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
        Button(onClick = { navigation.push(ComposeNestedResults.NestedSenderContainer) }) {
            Text(text = "Open Nested Sender Container")
        }
        Button(onClick = { resultChannel.push(ComposeNestedResults.Sender) }) {
            Text(text = "Get Result")
        }
    }
}

@NavigationDestination(ComposeNestedResults.NestedSenderContainer::class)
@Composable
fun ComposeNestedResultsNestedSenderContainer() {
    val container = rememberNavigationContainer(
        emptyBehavior = EmptyBehavior.CloseParent,
        filter = accept {
            key(ComposeNestedResults.Sender)
        },
    )
    TitledColumn(title = "Nested Sender Container") {
        Box(modifier = Modifier.fillMaxSize()) {
            container.Render()
        }
    }
}

@NavigationDestination(ComposeNestedResults.Sender::class)
@Composable
fun ComposeNestedResultsSender() {
    val navigation = navigationHandle<ComposeNestedResults.Sender>()
    TitledColumn(title = "Sender")  {
        Button(onClick = { navigation.closeWithResult("A") }) {
            Text(text = "Send A")
        }
        Button(onClick = { navigation.closeWithResult("B") }) {
            Text(text = "Send B")
        }
        Button(onClick = { navigation.requestClose() } ) {
            Text(text = "Close")
        }
    }
}
