/**
 * Enro Recipe: Request-Close Confirmation
 *
 * Demonstrates overriding a NavigationHandle's onCloseRequested callback to
 * prompt the user for confirmation when there are unsaved changes. The
 * "close vs requestClose" distinction matters here — pressing the back
 * button or tapping the back arrow calls requestClose, which routes through
 * the onCloseRequested callback we set up below. Programmatic dismissals
 * from inside that callback use close() directly, to avoid recursion.
 */
package dev.enro.recipes.requestclose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.complete
import dev.enro.configure
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.recipes.RecipeScaffold
import dev.enro.result.open
import dev.enro.result.registerForNavigationResult
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.navigationDestination
import dev.enro.ui.rememberNavigationContainer
import dev.enro.ui.scenes.dialog
import kotlinx.serialization.Serializable

// -- Root recipe key --

@Serializable
object RequestCloseConfirmationRecipe : NavigationKey

// -- Internal navigation keys --

@Serializable
data object EditDocumentHome : NavigationKey

@Serializable
data class EditDocument(val initial: String) : NavigationKey

@Serializable
data object ConfirmDiscardChangesDialog : NavigationKey.WithResult<Boolean>

// -- Recipe root --

@Composable
@NavigationDestination(RequestCloseConfirmationRecipe::class)
fun RequestCloseConfirmationRecipeScreen() {
    val navigation = navigationHandle<RequestCloseConfirmationRecipe>()
    RecipeScaffold(
        title = "Request-Close Confirmation",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(EditDocumentHome.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
        )
    }
}

// -- Home screen --

@Composable
@NavigationDestination(EditDocumentHome::class)
fun EditDocumentHomeDestination() {
    val navigation = navigationHandle<EditDocumentHome>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Open the editor and try pressing back. If you've made " +
                "changes you'll be asked to confirm discarding them; if you " +
                "haven't, the screen closes immediately.",
        )
        Button(onClick = { navigation.open(EditDocument(initial = "Hello, world.")) }) {
            Text("Open editor")
        }
    }
}

// -- The edit screen — overrides onCloseRequested --

@Composable
@NavigationDestination(EditDocument::class)
fun EditDocumentDestination() {
    val navigation = navigationHandle<EditDocument>()

    // Local draft state. Survives configuration change via rememberSaveable.
    var draft by rememberSaveable { mutableStateOf(navigation.key.initial) }
    val isDirty = draft != navigation.key.initial

    // The confirmation dialog returns Boolean: true = discard, false = keep editing.
    val confirmDiscard = registerForNavigationResult<Boolean>(
        onCompleted = { discard ->
            if (discard) navigation.close()   // user chose to discard — close directly.
            // If false, the user chose to keep editing — do nothing, stay on screen.
        },
    )

    // Override onCloseRequested. The Android back button, the top-bar arrow,
    // and any UI element that calls navigation.requestClose() will all route
    // through this callback.
    navigation.configure {
        onCloseRequested {
            if (!isDirty) {
                close()                       // nothing to discard — close directly.
            } else {
                confirmDiscard.open(ConfirmDiscardChangesDialog)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Edit the document below. Press back to test the confirmation flow.")
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Document content") },
        )
        Text(text = if (isDirty) "Unsaved changes." else "No changes.")
        Button(onClick = { navigation.complete() }) {
            // "Save" — semantically signals the task was completed.
            // We could surface a result type if a caller needed the new value.
            Text("Save and close")
        }
    }
}

// -- The confirmation dialog --

@NavigationDestination(ConfirmDiscardChangesDialog::class)
val confirmDiscardDialogDestination: NavigationDestinationProvider<ConfirmDiscardChangesDialog> =
    navigationDestination(
        metadata = {
            dialog(
                dialogProperties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true,
                ),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Discard changes?")
            Text("Your unsaved edits will be lost.")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = { navigation.complete(false) }) {
                    Text("Keep editing")
                }
                Button(onClick = { navigation.complete(true) }) {
                    Text("Discard")
                }
            }
        }
    }
