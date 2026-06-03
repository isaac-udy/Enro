package dev.enro.tests.application.compose.results

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.complete
import dev.enro.navigationHandle
import dev.enro.result.open
import dev.enro.result.registerForNavigationResult
import dev.enro.tests.application.compose.common.TitledColumn
import dev.enro.withMetadata
import kotlinx.serialization.Serializable

@Serializable
object ResultsWithMetadata : NavigationKey {
    @Serializable
    internal object Sender : NavigationKey.WithResult<String>

    object MetadataKey : NavigationKey.MetadataKey<String?>(null)
}


@NavigationDestination(ResultsWithMetadata::class)
@Composable
fun ResultsWithExtraScreen() {
    val lastResult = rememberSaveable { mutableStateOf("<No Result>") }
    val resultChannel = registerForNavigationResult<String> {
        require(instance.metadata.get(ResultsWithMetadata.MetadataKey) == it)
        lastResult.value = it
    }

    TitledColumn("Results With Extra") {
        Text("Last Result was \"${lastResult.value}\"")
        Button(onClick = {
            resultChannel.open(
                ResultsWithMetadata.Sender
                    .withMetadata(ResultsWithMetadata.MetadataKey, "A")
            )
        }) {
            Text("Request (A) as Result")
        }
        Button(onClick = {
            resultChannel.open(
                ResultsWithMetadata.Sender
                    .withMetadata(ResultsWithMetadata.MetadataKey, "B")
            )
        }) {
            Text("Request (B) as Result")
        }
        Button(onClick = {
            resultChannel.open(
                ResultsWithMetadata.Sender
                    .withMetadata(ResultsWithMetadata.MetadataKey, "C")
            )
        }) {
            Text("Request (C) as Result")
        }
    }
}

@NavigationDestination(ResultsWithMetadata.Sender::class)
@Composable
fun ResultsWithExtraSenderScreen() {
    val navigationHandle = navigationHandle<ResultsWithMetadata.Sender>()
    val instruction = navigationHandle.instance
    val actualResultExtra = instruction.metadata.get(ResultsWithMetadata.MetadataKey)!!

    TitledColumn("Results With Extra") {
        Text("Extra for \"actualResult\" is \"$actualResultExtra\"")
        Button(onClick = {
            navigationHandle.complete(actualResultExtra)
        }) {
            Text("Send Result")
        }
    }
}
