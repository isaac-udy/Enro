/**
 * Enro Recipe: Destination Registration
 *
 * Nav3 equivalent: "Entry Provider DSL" recipe
 * https://nicbell.github.io/nav3/recipes/entry-provider
 *
 * Demonstrates how Enro registers destinations (both annotation-based and programmatic),
 * compared to Nav3's entryProvider DSL.
 *
 * Key differences from Nav3:
 * - Nav3 uses an inline entryProvider lambda within NavDisplay to map keys to content.
 *   All destinations must be known at the call site.
 * - Enro uses @NavigationDestination annotations processed by KSP at compile time, generating
 *   NavigationModule classes that register bindings. Destinations can be in any module.
 * - Enro also supports programmatic registration via `navigationDestination` val declarations
 *   annotated with @NavigationDestination, useful for dialogs, overlays, and managed flows.
 * - Nav3's entryProvider is essentially a big when/if block. Enro's approach is declarative
 *   and distributed across the codebase.
 */
package dev.enro.recipes.entryprovider

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.close
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.navigationDestination
import dev.enro.ui.scenes.dialog
import dev.enro.ui.scenes.directOverlay
import kotlinx.serialization.Serializable

// ============================================================
// Style 1: Annotation-based registration (@Composable function)
// ============================================================
// This is the most common approach. The KSP processor generates a NavigationModule
// that binds the key type to this composable.

@Serializable
data object DashboardScreen : NavigationKey

// Nav3 equivalent: Inside entryProvider { entry -> when(entry.key) { is DashboardScreen -> { ... } } }
// Enro: Standalone composable, discovered at compile time.
@Composable
@NavigationDestination(DashboardScreen::class)
fun DashboardScreenDestination() {
    val navigation = navigationHandle<DashboardScreen>()
    Column {
        Text("Dashboard")
        Button(onClick = { navigation.open(ConfirmDialog) }) {
            Text("Show Confirm Dialog")
        }
        Button(onClick = { navigation.open(InfoSheet) }) {
            Text("Show Info Sheet")
        }
    }
}

// ============================================================
// Style 2: Programmatic registration (val + navigationDestination)
// ============================================================
// Useful when you need to configure metadata (dialog, directOverlay, etc.)
// or when the destination is a managed flow.

@Serializable
data object ConfirmDialog : NavigationKey

// The `navigationDestination` function creates a NavigationDestinationProvider.
// The `metadata` block configures how this destination is displayed (as a dialog, overlay, etc.)
// Nav3 equivalent: entryProvider with Dialog() wrapping inside the content lambda.
@NavigationDestination(ConfirmDialog::class)
val confirmDialogDestination: NavigationDestinationProvider<ConfirmDialog> = navigationDestination(
    metadata = {
        // This tells Enro's DialogSceneStrategy to render this in a Dialog composable.
        dialog(
            dialogProperties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
            )
        )
    }
) {
    // Inside the NavigationDestinationScope, `navigation` is available directly.
    val navigation = navigationHandle<ConfirmDialog>()
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

// Direct overlay: renders on top of the current scene without a Dialog wrapper.
// The destination is responsible for its own overlay UI (e.g., ModalBottomSheet).
@NavigationDestination(InfoSheet::class)
val infoSheetDestination: NavigationDestinationProvider<InfoSheet> = navigationDestination(
    metadata = {
        directOverlay()
    }
) {
    val navigation = navigationHandle<InfoSheet>()
    // In a directOverlay, you control the full rendering.
    // Typically used with ModalBottomSheet or custom overlay UIs.
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

// ============================================================
// Style 3: Conditional metadata based on the key
// ============================================================

@Serializable
data class ItemDetail(val itemId: String, val showAsDialog: Boolean = false) : NavigationKey

@NavigationDestination(ItemDetail::class)
val itemDetailDestination: NavigationDestinationProvider<ItemDetail> = navigationDestination(
    metadata = {
        // The MetadataBuilder has access to the NavigationKey.Instance, so you can
        // conditionally apply metadata based on the key's properties.
        if (key.showAsDialog) {
            dialog()
        }
    }
) {
    val navigation = navigationHandle<ItemDetail>()
    Column {
        Text("Item: ${navigation.key.itemId}")
        Button(onClick = { navigation.close() }) {
            Text("Close")
        }
    }
}
