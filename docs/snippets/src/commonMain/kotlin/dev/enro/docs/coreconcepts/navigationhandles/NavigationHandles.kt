// Snippets for docs/ghpages/docs/core-concepts/navigation-handles.md
package dev.enro.docs.coreconcepts.navigationhandles

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.close
import dev.enro.complete
import dev.enro.configure
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.requestClose
import dev.enro.result.open
import dev.enro.result.registerForNavigationResult
import dev.enro.ui.navigationDestination
import dev.enro.ui.scenes.dialog
import dev.enro.withMetadata
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

private val today = LocalDate(2026, 1, 1)

@Serializable
data class ShowProfile(val userId: String) : NavigationKey

@Serializable
data class SelectDate(val maxDate: LocalDate? = null) : NavigationKey.WithResult<LocalDate>

@Serializable
data object ConfirmDialog : NavigationKey

@Serializable
data class EditProfile(val initial: String) : NavigationKey

@Serializable
data object ConfirmDiscardDialog : NavigationKey.WithResult<Boolean>

@Serializable
data class ConfirmDelete(val itemName: String) : NavigationKey

object IsExpanded : NavigationKey.MetadataKey<Boolean>(default = false)

// region docs:navigation-handles/composable
@Composable
@NavigationDestination(ShowProfile::class)
fun ProfileScreen() {
    val navigation = navigationHandle<ShowProfile>()
    Text("Profile for ${navigation.key.userId}")
}
// endregion

@Composable
fun UntypedHandle() {
    // region docs:navigation-handles/untyped
    val navigation = navigationHandle<NavigationKey>()
    // endregion
}

// region docs:navigation-handles/view-model
class ProfileViewModel : ViewModel() {
    val navigation by navigationHandle<ShowProfile>()
}
// endregion

// region docs:navigation-handles/provider
@NavigationDestination(ConfirmDialog::class)
val confirmDialogDestination = navigationDestination<ConfirmDialog>(
    metadata = { dialog() }
) {
    AlertDialog(
        onDismissRequest = { navigation.close() },
        confirmButton = { Button(onClick = { navigation.close() }) { Text("OK") } },
        title = { Text("Confirm") },
        text = { Text("Opened with ${navigation.key}") },
    )
}
// endregion

fun openOperations(navigation: NavigationHandle<*>) {
    // region docs:navigation-handles/open
    navigation.open(SelectDate(maxDate = today))
    navigation.open(ShowProfile("user-123").withMetadata(IsExpanded, true))
    // endregion
}

fun closeOperations(navigation: NavigationHandle<*>) {
    // region docs:navigation-handles/close
    navigation.close()             // close this destination
    navigation.requestClose()      // ask this destination to close
    // endregion
}

// region docs:navigation-handles/configure
@Composable
@NavigationDestination(EditProfile::class)
fun EditProfileScreen() {
    val navigation = navigationHandle<EditProfile>()
    var draft by remember { mutableStateOf(navigation.key.initial) }

    val confirmDiscard = registerForNavigationResult<Boolean>(
        onCompleted = { discard -> if (discard) navigation.close() },
    )

    navigation.configure {
        onCloseRequested {
            if (draft == navigation.key.initial) {
                close()              // nothing changed — close directly
            } else {
                confirmDiscard.open(ConfirmDiscardDialog)
            }
        }
    }

    // ...
}
// endregion

// region docs:navigation-handles/configure-view-model
class EditProfileViewModel : ViewModel() {
    val draft = MutableStateFlow("")
    private val originalValue: String get() = navigation.key.initial

    val navigation by navigationHandle<EditProfile> {
        onCloseRequested {
            if (draft.value == originalValue) {
                close()
            } else {
                // delegate to a UI-side handler, emit an event, etc.
                viewModelScope.launch { closeRequests.emit(Unit) }
            }
        }
    }

    val closeRequests = MutableSharedFlow<Unit>()
}
// endregion

fun completeWithResult(navigation: NavigationHandle<SelectDate>) {
    // region docs:navigation-handles/complete-with-result
    navigation.complete(today)
    // endregion
}

// region docs:navigation-handles/complete-without-result
@Composable
@NavigationDestination(ConfirmDelete::class)
fun ConfirmDeleteDialog() {
    val navigation = navigationHandle<ConfirmDelete>()
    AlertDialog(
        onDismissRequest = { navigation.requestClose() }, // user backed out
        confirmButton = {
            Button(onClick = { navigation.complete() }) { Text("Delete") } // confirmed
        },
        dismissButton = {
            Button(onClick = { navigation.close() }) { Text("Cancel") }    // cancelled
        },
    )
}
// endregion
