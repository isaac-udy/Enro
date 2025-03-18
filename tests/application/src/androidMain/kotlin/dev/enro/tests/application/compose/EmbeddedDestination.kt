package dev.enro.tests.application.compose

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.enro.annotations.ExperimentalEnroApi
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.close
import dev.enro.core.closeWithResult
import dev.enro.core.compose.navigationHandle
import dev.enro.destination.compose.EmbeddedNavigationDestination
import dev.enro.tests.application.compose.common.TitledColumn
import kotlinx.parcelize.Parcelize

@Parcelize
object EmbeddedDestination : NavigationKey.SupportsPush {
    @Parcelize
    internal data object NoResult: NavigationKey.SupportsPush

    @Parcelize
    internal data object WithResult: NavigationKey.SupportsPush.WithResult<String>
}

@OptIn(ExperimentalEnroApi::class)
@Composable
@NavigationDestination(EmbeddedDestination::class)
fun EmbeddedDestination() {
    val navigation = navigationHandle()
    var withResult by remember {
        mutableStateOf(false)
    }

    var lastResult by remember {
        mutableStateOf("(None)")
    }

    TitledColumn(title = "Embedded Destination") {

        Text(text = "Last result: $lastResult")

        Button(onClick = { withResult = !withResult }) {
            Text(text = "Toggle Result")
        }

        Crossfade(
            modifier = Modifier.weight(1f),
            targetState = withResult
        ) { withResult ->
            if (withResult) {
                EmbeddedNavigationDestination(
                    navigationKey = EmbeddedDestination.WithResult,
                    modifier = Modifier.fillMaxSize(),
                    onClosed = {
                        navigation.close()
                    },
                    onResult = {
                        lastResult = it
                    }
                )
            }
            else {
                EmbeddedNavigationDestination(
                    navigationKey = EmbeddedDestination.NoResult,
                    onClosed = {
                        navigation.close()
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
@NavigationDestination(EmbeddedDestination.NoResult::class)
fun EmbeddedDestinationNoResult() {
    val navigation = navigationHandle()
    TitledColumn(title = "No Result Destination") {
        Text(text = "This destination has no result")
        Button(onClick = { navigation.close() }) {
            Text(text = "Close")
        }
    }
}

@Composable
@NavigationDestination(EmbeddedDestination.WithResult::class)
fun EmbeddedDestinationWithResult() {
    val navigation = navigationHandle<EmbeddedDestination.WithResult>()
    TitledColumn(title = "No Result Destination") {
        Text(text = "This destination can return results")
        Button(onClick = { navigation.closeWithResult("A") }) {
            Text(text = "Send Result (A)")
        }
        Button(onClick = { navigation.closeWithResult("B") }) {
            Text(text = "Send Result (B)")
        }
        Button(onClick = { navigation.close() }) {
            Text(text = "Close")
        }
    }
}