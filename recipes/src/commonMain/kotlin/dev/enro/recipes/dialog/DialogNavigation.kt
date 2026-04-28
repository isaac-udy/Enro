/**
 * Enro Recipe: Dialog Navigation
 *
 * Demonstrates how to show dialogs as navigation destinations.
 */
package dev.enro.recipes.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.recipes.RecipeScaffold
import dev.enro.requestClose
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.navigationDestination
import dev.enro.ui.rememberNavigationContainer
import dev.enro.ui.scenes.dialog
import dev.enro.ui.scenes.directOverlay
import kotlinx.serialization.Serializable

@Serializable
object DialogRecipe : NavigationKey

@Serializable
data object DialogMainScreen : NavigationKey

@Serializable
data class ConfirmDeleteDialog(val itemName: String) : NavigationKey

@Serializable
data class CustomStyledDialog(val title: String, val message: String) : NavigationKey

@Serializable
data class FullScreenDialog(val content: String) : NavigationKey

@Serializable
data class AlertOverlay(val message: String) : NavigationKey

@Composable
@NavigationDestination(DialogRecipe::class)
fun DialogRecipeScreen() {
    val navigation = navigationHandle<DialogRecipe>()
    RecipeScaffold(
        title = "Dialog Navigation",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(DialogMainScreen.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
        )
    }
}

// Style 1: dialog metadata

@NavigationDestination(ConfirmDeleteDialog::class)
val confirmDeleteDialogDestination: NavigationDestinationProvider<ConfirmDeleteDialog> = navigationDestination(
    metadata = {
        dialog()
    }
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
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

// Style 2: custom dialog properties

@NavigationDestination(CustomStyledDialog::class)
val customStyledDialogDestination: NavigationDestinationProvider<CustomStyledDialog> = navigationDestination(
    metadata = {
        dialog(
            dialogProperties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false,
            )
        )
    }
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(navigation.key.title)
            Text(navigation.key.message)
            Button(onClick = { navigation.close() }) {
                Text("OK")
            }
        }
    }
}

// Style 3: full-screen dialog

@NavigationDestination(FullScreenDialog::class)
val fullScreenDialogDestination: NavigationDestinationProvider<FullScreenDialog> = navigationDestination(
    metadata = {
        dialog(
            dialogProperties = DialogProperties(
                usePlatformDefaultWidth = false,
            )
        )
    }
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(navigation.key.content)
        Button(onClick = { navigation.close() }) {
            Text("Close")
        }
    }
}

// Style 4: direct overlay with AlertDialog

@NavigationDestination(AlertOverlay::class)
val alertOverlayDestination: NavigationDestinationProvider<AlertOverlay> = navigationDestination(
    metadata = {
        directOverlay()
    }
) {
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

@Composable
@NavigationDestination(DialogMainScreen::class)
fun DialogMainScreenDestination() {
    val navigation = navigationHandle<DialogMainScreen>()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
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
