/**
 * Enro Recipe: Destination Registration
 *
 * Demonstrates Enro's annotation-based and provider-based destination registration.
 */
package dev.enro.recipes.entryprovider

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.navigationDestination
import dev.enro.ui.rememberNavigationContainer
import dev.enro.ui.scenes.dialog
import dev.enro.ui.scenes.directOverlay
import kotlinx.serialization.Serializable

// -- Root recipe key --

@Serializable
object DestinationRegistrationRecipe : NavigationKey

@Composable
@NavigationDestination(DestinationRegistrationRecipe::class)
fun DestinationRegistrationRecipeScreen() {
    val navigation = navigationHandle<DestinationRegistrationRecipe>()
    RecipeScaffold(
        title = "Destination Registration",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(DashboardScreen.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
        )
    }
}

// Style 1: Annotation-based registration (composable function)

@Serializable
data object DashboardScreen : NavigationKey

@Composable
@NavigationDestination(DashboardScreen::class)
fun DashboardScreenDestination() {
    val navigation = navigationHandle<DashboardScreen>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Dashboard")
        Button(onClick = { navigation.open(ConfirmDialog) }) {
            Text("Show Confirm Dialog")
        }
        Button(onClick = { navigation.open(InfoSheet) }) {
            Text("Show Info Sheet")
        }
        Button(onClick = { navigation.open(EntryItemDetail("item-1", showAsDialog = false)) }) {
            Text("Show Item (full screen)")
        }
        Button(onClick = { navigation.open(EntryItemDetail("item-1", showAsDialog = true)) }) {
            Text("Show Item (dialog)")
        }
    }
}

// Style 2: Programmatic registration (val + navigationDestination)

@Serializable
data object ConfirmDialog : NavigationKey

@NavigationDestination(ConfirmDialog::class)
val confirmDialogDestination: NavigationDestinationProvider<ConfirmDialog> = navigationDestination(
    metadata = {
        dialog(
            dialogProperties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
            )
        )
    }
) {
    AlertDialog(
        onDismissRequest = { navigation.close() },
        title = { Text("Confirm") },
        text = { Text("Are you sure?") },
        confirmButton = {
            TextButton(onClick = { navigation.close() }) {
                Text("Yes")
            }
        },
        dismissButton = {
            TextButton(onClick = { navigation.close() }) {
                Text("No")
            }
        },
    )
}

@Serializable
data object InfoSheet : NavigationKey

@NavigationDestination(InfoSheet::class)
val infoSheetDestination: NavigationDestinationProvider<InfoSheet> = navigationDestination(
    metadata = {
        directOverlay()
    }
) {
    AlertDialog(
        onDismissRequest = { navigation.close() },
        title = { Text("Information") },
        text = { Text("This is rendered as a direct overlay.") },
        confirmButton = {
            TextButton(onClick = { navigation.close() }) {
                Text("OK")
            }
        },
    )
}

// Style 3: Conditional metadata based on the key

@Serializable
data class EntryItemDetail(val itemId: String, val showAsDialog: Boolean = false) : NavigationKey

@NavigationDestination(EntryItemDetail::class)
val itemDetailDestination: NavigationDestinationProvider<EntryItemDetail> = navigationDestination(
    metadata = {
        if (key.showAsDialog) {
            dialog()
        }
    }
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Item: ${navigation.key.itemId}")
        Button(onClick = { navigation.close() }) {
            Text("Close")
        }
    }
}
