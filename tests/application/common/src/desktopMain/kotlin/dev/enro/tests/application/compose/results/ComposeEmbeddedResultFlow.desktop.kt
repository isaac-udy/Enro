package dev.enro.tests.application.compose.results

import androidx.compose.material.Button
import androidx.compose.material.Text
import dev.enro.annotations.NavigationDestination
import dev.enro.complete
import dev.enro.completeFrom
import dev.enro.navigationHandle
import dev.enro.tests.application.compose.common.TitledColumn
import dev.enro.ui.destinations.rootWindowDestination

@NavigationDestination(ComposeEmbeddedResultFlow.Activity::class)
internal val composeEmbeddedResultFlowWindow = rootWindowDestination<ComposeEmbeddedResultFlow.Activity> {
    val navigation = navigationHandle<ComposeEmbeddedResultFlow.Activity>()

    TitledColumn(title = "Embedded Result Flow Activity") {
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