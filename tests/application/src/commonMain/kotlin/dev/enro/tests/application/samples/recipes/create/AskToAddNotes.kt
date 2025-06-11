package dev.enro.tests.application.samples.recipes.create

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.close
import dev.enro.complete
import dev.enro.navigationHandle
import dev.enro.ui.navigationDestination
import dev.enro.ui.scenes.DirectOverlaySceneStrategy
import kotlinx.serialization.Serializable

@Serializable
object AskToAddNotes : NavigationKey.WithResult<Boolean>

@NavigationDestination(AskToAddNotes::class)
val askToAddNotesDialog = navigationDestination<AskToAddNotes>(
    metadata = mapOf(
        DirectOverlaySceneStrategy.overlay()
    )
) {
    val navigation = navigationHandle<AskToAddNotes>()

    AlertDialog(
        onDismissRequest = { navigation.close() },
        title = { Text("Add Notes?") },
        text = { Text("Would you like to add any special notes, tips, or variations for this recipe?") },
        confirmButton = {
            TextButton(onClick = { navigation.complete(true) }) {
                Text("Yes, add notes")
            }
        },
        dismissButton = {
            TextButton(onClick = { navigation.complete(false) }) {
                Text("No thanks")
            }
        }
    )
}