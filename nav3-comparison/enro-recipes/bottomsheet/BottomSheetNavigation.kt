/**
 * Enro Recipe: Bottom Sheet Navigation
 *
 * Nav3 equivalent: "Bottom Sheet" recipe
 * https://nicbell.github.io/nav3/recipes/bottom-sheet
 *
 * Demonstrates how to show ModalBottomSheet as a navigation destination in Enro,
 * compared to Nav3's BottomSheetScene approach.
 *
 * Key differences from Nav3:
 * - Nav3 requires BottomSheetScene in the entryProvider, which wraps content in
 *   a ModalBottomSheet and manages dismiss animations.
 * - Enro uses `directOverlay()` metadata, which renders the destination on top of the
 *   current scene without any wrapping. The destination itself uses ModalBottomSheet.
 * - Enro does not have a built-in BottomSheetScene (unlike Dialog which has DialogSceneStrategy).
 *   Instead, directOverlay() gives full control to the destination, which is more flexible.
 * - The key advantage: the bottom sheet is a proper navigation destination on the backstack,
 *   so it survives configuration changes and supports back gestures.
 */
package dev.enro.recipes.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.close
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.requestClose
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.navigationDestination
import dev.enro.ui.scenes.directOverlay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

// -- Navigation Keys --

@Serializable
data object SheetHost : NavigationKey

@Serializable
data class OptionsSheet(val title: String) : NavigationKey

@Serializable
data class DetailSheet(val itemId: String) : NavigationKey

// -- Bottom Sheet Destinations --
// Nav3 equivalent: entryProvider { BottomSheetScene(key) { content } }
// Enro: Use directOverlay() + ModalBottomSheet in the destination content.

@OptIn(ExperimentalMaterial3Api::class)
@NavigationDestination(OptionsSheet::class)
val optionsSheetDestination: NavigationDestinationProvider<OptionsSheet> = navigationDestination(
    metadata = {
        // directOverlay() renders this destination on top of the current scene.
        // The destination is responsible for its own overlay UI.
        directOverlay()
    }
) {
    val navigation = navigationHandle<OptionsSheet>()
    val sheetState = rememberModalBottomSheetState()

    // ModalBottomSheet creates its own overlay/scrim.
    // When dismissed, we close the navigation destination.
    ModalBottomSheet(
        onDismissRequest = { navigation.requestClose() },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(navigation.key.title)

            Button(
                onClick = { navigation.close() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Option A")
            }

            Button(
                onClick = { navigation.close() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Option B")
            }

            Button(
                onClick = { navigation.close() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Option C")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@NavigationDestination(DetailSheet::class)
val detailSheetDestination: NavigationDestinationProvider<DetailSheet> = navigationDestination(
    metadata = {
        directOverlay()
    }
) {
    val navigation = navigationHandle<DetailSheet>()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = { navigation.requestClose() },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
        ) {
            Text("Detail for item: ${navigation.key.itemId}")
            Text("This sheet is fully expanded by default.")

            Button(onClick = {
                // Animate hide, then close the navigation destination
                scope.launch {
                    sheetState.hide()
                    navigation.close()
                }
            }) {
                Text("Done")
            }
        }
    }
}

// -- Host --

@Composable
@NavigationDestination(SheetHost::class)
fun SheetHostDestination() {
    val navigation = navigationHandle<SheetHost>()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Text("Bottom Sheet Examples")

        Button(onClick = { navigation.open(OptionsSheet("Choose an option")) }) {
            Text("Show Options Sheet")
        }

        Button(onClick = { navigation.open(DetailSheet("item-42")) }) {
            Text("Show Detail Sheet (fully expanded)")
        }
    }
}
