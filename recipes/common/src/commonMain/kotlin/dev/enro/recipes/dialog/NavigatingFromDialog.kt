/**
 * Enro Recipe: Navigating From a Dialog
 *
 * Demonstrates what happens when a dialog destination navigates:
 *  - `navigation.open(key)` from a dialog puts the new destination on top of
 *    the dialog. The dialog is still on the backstack, so Back returns to it.
 *  - `navigation.closeAndReplaceWith(key)` removes the dialog and opens the
 *    new destination in its place. Back returns to the screen under the dialog.
 *  - A dialog can open another dialog; they stack like any other destinations.
 *
 * A dialog is an ordinary destination with `dialog()` metadata, so every
 * NavigationHandle operation works from inside it.
 */
package dev.enro.recipes.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.closeAndReplaceWith
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.recipes.RecipeScaffold
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.navigationDestination
import dev.enro.ui.rememberNavigationContainer
import dev.enro.ui.scenes.dialog
import kotlinx.serialization.Serializable

@Serializable
object NavigatingFromDialogRecipe : NavigationKey

@Serializable
data object FromDialogHome : NavigationKey

@Serializable
data object ChooseActionDialog : NavigationKey

@Serializable
data object AreYouSureDialog : NavigationKey

@Serializable
data class FromDialogDetail(val openedBy: String) : NavigationKey

@Composable
@NavigationDestination(NavigatingFromDialogRecipe::class)
fun NavigatingFromDialogRecipeScreen() {
    val navigation = navigationHandle<NavigatingFromDialogRecipe>()
    RecipeScaffold(
        title = "Navigating From a Dialog",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(FromDialogHome.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
        )
    }
}

@Composable
@NavigationDestination(FromDialogHome::class)
fun FromDialogHomeDestination() {
    val navigation = navigationHandle<FromDialogHome>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Home", style = MaterialTheme.typography.titleLarge)
        Button(onClick = { navigation.open(ChooseActionDialog) }) {
            Text("Open dialog")
        }
    }
}

@NavigationDestination(ChooseActionDialog::class)
val chooseActionDialogDestination: NavigationDestinationProvider<ChooseActionDialog> = navigationDestination(
    metadata = { dialog() },
) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Choose an action", style = MaterialTheme.typography.titleMedium)

            // The detail screen opens on top of this dialog. Back from the
            // detail screen returns here.
            Button(onClick = { navigation.open(FromDialogDetail(openedBy = "open")) }) {
                Text("Open a screen on top")
            }

            // This dialog is removed and the detail screen takes its place.
            // Back from the detail screen returns to Home.
            Button(onClick = { navigation.closeAndReplaceWith(FromDialogDetail(openedBy = "closeAndReplaceWith")) }) {
                Text("Replace this dialog with a screen")
            }

            // A second dialog, stacked on this one.
            Button(onClick = { navigation.open(AreYouSureDialog) }) {
                Text("Open another dialog")
            }

            TextButton(onClick = { navigation.close() }) {
                Text("Cancel")
            }
        }
    }
}

@NavigationDestination(AreYouSureDialog::class)
val areYouSureDialogDestination: NavigationDestinationProvider<AreYouSureDialog> = navigationDestination(
    metadata = { dialog() },
) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Are you sure?", style = MaterialTheme.typography.titleMedium)
            Text("Closing this dialog returns to the first dialog.")
            TextButton(onClick = { navigation.close() }) {
                Text("Close")
            }
        }
    }
}

@Composable
@NavigationDestination(FromDialogDetail::class)
fun FromDialogDetailDestination() {
    val navigation = navigationHandle<FromDialogDetail>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Detail", style = MaterialTheme.typography.titleLarge)
        Text("Opened from the dialog with ${navigation.key.openedBy}.")
        Button(onClick = { navigation.close() }) {
            Text("Back")
        }
    }
}
