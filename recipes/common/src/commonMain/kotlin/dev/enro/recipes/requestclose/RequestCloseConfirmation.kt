/**
 * Enro Recipe: Request-Close Confirmation
 *
 * Shows an `onCloseRequested` callback on a NavigationHandle that opens a
 * confirmation dialog when the screen has unsaved changes.
 *
 * System back and predictive back call `requestClose()`, which runs the
 * `onCloseRequested` callback in place of the close. `close()` does not run
 * the callback, so the callback calls `close()` to close the screen.
 */
package dev.enro.recipes.requestclose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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

@Serializable
object RequestCloseConfirmationRecipe : NavigationKey

@Serializable
data object EditDocumentHome : NavigationKey

@Serializable
data class EditDocument(val initial: String) : NavigationKey

@Serializable
data object ConfirmDiscardChangesDialog : NavigationKey.WithResult<Boolean>

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

@Composable
@NavigationDestination(EditDocument::class)
fun EditDocumentDestination() {
    val navigation = navigationHandle<EditDocument>()

    var draft by rememberSaveable { mutableStateOf(navigation.key.initial) }
    val isDirty = draft != navigation.key.initial

    // ConfirmDiscardChangesDialog completes with true to discard the changes
    // and false to continue editing.
    val confirmDiscard = registerForNavigationResult<Boolean>(
        onCompleted = { discard ->
            // close() does not run onCloseRequested, so the dialog does not open again.
            if (discard) navigation.close()
        },
    )

    // System back and each call to navigation.requestClose() run this
    // callback in place of the close.
    navigation.configure {
        onCloseRequested {
            if (!isDirty) {
                close()
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
        // complete() closes the screen without running onCloseRequested.
        Button(onClick = { navigation.complete() }) {
            Text("Save and close")
        }
    }
}

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
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
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
    }
