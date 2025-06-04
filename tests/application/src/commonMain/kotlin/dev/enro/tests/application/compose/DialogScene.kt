package dev.enro.tests.application.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.close
import dev.enro.complete
import dev.enro.tests.application.compose.common.TitledColumn
import dev.enro.ui.navigationDestination
import dev.enro.ui.scenes.DialogSceneStrategy
import kotlinx.serialization.Serializable

@Serializable
object DialogScene : NavigationKey

@NavigationDestination(DialogScene::class)
val dialogScene = navigationDestination<DialogScene>(
    metadata = mapOf(
        DialogSceneStrategy.dialog()
    )
) {
    Card {
        TitledColumn(
            title = "Dialog",
            modifier = Modifier,
        ) {
            Text("This is a dialog")
            Row(
                modifier = Modifier
                    .padding(start = 56.dp, top = 16.dp)
                    .align(Alignment.End),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TextButton(onClick = { navigation.close() }) {
                    Text("Close")
                }
                TextButton(onClick = { navigation.complete() }) {
                    Text("Complete")
                }
            }
        }
    }
}