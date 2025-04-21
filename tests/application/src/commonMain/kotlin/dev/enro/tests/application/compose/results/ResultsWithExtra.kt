package dev.enro.tests.application.compose.results

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.closeWithResult
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.registerForNavigationResult
import dev.enro.core.withExtra
import dev.enro.tests.application.compose.common.TitledColumn
import kotlinx.serialization.Serializable

@Serializable
object ResultsWithExtra : NavigationKey.SupportsPush {
    @Serializable
    internal object Sender : NavigationKey.SupportsPush.WithResult<String>
}


@NavigationDestination(ResultsWithExtra::class)
@Composable
fun ResultsWithExtraScreen() {
    val lastResult = rememberSaveable { mutableStateOf("<No Result>") }
    val resultChannel = registerForNavigationResult<String> {
        require(instruction.extras.get<String>("actualResult") == it)
        lastResult.value = it
    }

    TitledColumn("Results With Extra") {
        Text("Last Result was \"${lastResult.value}\"")
        Button(onClick = {
            resultChannel.push(
                ResultsWithExtra.Sender
                    .withExtra("actualResult", "A")
            )
        }) {
            Text("Request (A) as Result")
        }
        Button(onClick = {
            resultChannel.push(
                ResultsWithExtra.Sender
                    .withExtra("actualResult", "B")
            )
        }) {
            Text("Request (B) as Result")
        }
        Button(onClick = {
            resultChannel.push(
                ResultsWithExtra.Sender
                    .withExtra("actualResult", "C")
            )
        }) {
            Text("Request (C) as Result")
        }
    }
}

@NavigationDestination(ResultsWithExtra.Sender::class)
@Composable
fun ResultsWithExtraSenderScreen() {
    val navigationHandle = navigationHandle<ResultsWithExtra.Sender>()
    val instruction = navigationHandle.instruction
    val actualResultExtra = instruction.extras.get<String>("actualResult")!!

    TitledColumn("Results With Extra") {
        Text("Extra for \"actualResult\" is \"$actualResultExtra\"")
        Button(onClick = {
            navigationHandle.closeWithResult(actualResultExtra)
        }) {
            Text("Send Result")
        }
    }
}
