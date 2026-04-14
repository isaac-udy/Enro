/**
 * Enro Recipe: Dialog Navigation
 *
 * Nav3 equivalent: "Dialog" recipe
 * https://nicbell.github.io/nav3/recipes/dialog
 *
 * Demonstrates how to show dialogs as navigation destinations in Enro,
 * compared to Nav3's Dialog scene approach.
 *
 * Key differences from Nav3:
 * - Nav3 requires wrapping content in a DialogScene within the entryProvider DSL.
 * - Enro uses `dialog()` metadata on the destination, which tells the DialogSceneStrategy
 *   to render the content inside a Dialog composable automatically.
 * - Enro also supports `directOverlay()` for cases where you want full control over the
 *   overlay rendering (e.g., using AlertDialog directly).
 * - Both approaches are navigation-aware: the dialog is part of the backstack and can be
 *   closed via back gestures, navigation.close(), or clicking outside.
 */
package dev.enro.recipes.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.close
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.requestClose
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.navigationDestination
import dev.enro.ui.scenes.dialog
import dev.enro.ui.scenes.directOverlay
import kotlinx.serialization.Serializable

// -- Navigation Keys --

@Serializable
data object MainScreen : NavigationKey

@Serializable
data class ConfirmDeleteDialog(val itemName: String) : NavigationKey

@Serializable
data class CustomStyledDialog(val title: String, val message: String) : NavigationKey

@Serializable
data class FullScreenDialog(val content: String) : NavigationKey

@Serializable
data class AlertOverlay(val message: String) : NavigationKey

// -- Style 1: Dialog via metadata (most common) --
// Nav3 equivalent: entryProvider { DialogScene(key) { content } }
// Enro wraps the content in a Dialog composable automatically.
// You only provide the dialog's inner content (e.g., a Card).

@NavigationDestination(ConfirmDeleteDialog::class)
val confirmDeleteDialogDestination: NavigationDestinationProvider<ConfirmDeleteDialog> = navigationDestination(
    metadata = {
        // dialog() tells the DialogSceneStrategy to wrap this in Dialog(...)
        dialog()
    }
) {
    // The content here is rendered INSIDE the Dialog composable.
    // Dialog dismiss is handled automatically when the user clicks outside
    // or presses back (it triggers a close on the navigation instance).
    val navigation = navigationHandle<ConfirmDeleteDialog>()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Delete ${navigation.key.itemName}?")
            Text("This action cannot be undone.")

            Button(onClick = { navigation.close() }) {
                Text("Delete")
            }
            TextButton(onClick = { navigation.close() }) {
                Text("Cancel")
            }
        }
    }
}

// -- Style 2: Dialog with custom properties --
// You can pass DialogProperties to customize behavior.

@NavigationDestination(CustomStyledDialog::class)
val customStyledDialogDestination: NavigationDestinationProvider<CustomStyledDialog> = navigationDestination(
    metadata = {
        dialog(
            dialogProperties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false, // user must explicitly dismiss
                usePlatformDefaultWidth = false, // allow custom width
            )
        )
    }
) {
    val navigation = navigationHandle<CustomStyledDialog>()
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(navigation.key.title)
            Text(navigation.key.message)
            Button(onClick = { navigation.close() }) {
                Text("OK")
            }
        }
    }
}

// -- Style 3: Full-screen dialog --
// For full-screen dialogs, use DialogProperties with decorFitsSystemWindows = false.

@NavigationDestination(FullScreenDialog::class)
val fullScreenDialogDestination: NavigationDestinationProvider<FullScreenDialog> = navigationDestination(
    metadata = {
        dialog(
            dialogProperties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            )
        )
    }
) {
    val navigation = navigationHandle<FullScreenDialog>()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
    ) {
        Text(navigation.key.content)
        Button(onClick = { navigation.close() }) {
            Text("Close")
        }
    }
}

// -- Style 4: Direct overlay with AlertDialog --
// When using directOverlay(), the destination controls the full rendering.
// This is useful when you want to use AlertDialog (which creates its own Dialog internally)
// or any other overlay composable.

@NavigationDestination(AlertOverlay::class)
val alertOverlayDestination: NavigationDestinationProvider<AlertOverlay> = navigationDestination(
    metadata = {
        directOverlay()
    }
) {
    val navigation = navigationHandle<AlertOverlay>()
    AlertDialog(
        onDismissRequest = { navigation.requestClose() },
        title = { Text("Alert") },
        text = { Text(navigation.key.message) },
        confirmButton = {
            TextButton(onClick = { navigation.close() }) {
                Text("OK")
            }
        },
    )
}

// -- Host --

@Composable
@NavigationDestination(MainScreen::class)
fun MainScreenDestination() {
    val navigation = navigationHandle<MainScreen>()

    Column {
        Text("Dialog Examples")

        Button(onClick = { navigation.open(ConfirmDeleteDialog("My Document")) }) {
            Text("Show Confirm Delete (dialog metadata)")
        }

        Button(onClick = { navigation.open(CustomStyledDialog("Warning", "This is important.")) }) {
            Text("Show Custom Dialog (non-dismissible outside)")
        }

        Button(onClick = { navigation.open(FullScreenDialog("Full screen content here")) }) {
            Text("Show Full Screen Dialog")
        }

        Button(onClick = { navigation.open(AlertOverlay("Something happened!")) }) {
            Text("Show Alert (direct overlay)")
        }
    }
}
