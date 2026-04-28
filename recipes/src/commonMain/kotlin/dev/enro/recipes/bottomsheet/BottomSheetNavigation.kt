/**
 * Enro Recipe: Bottom Sheet Navigation
 *
 * Demonstrates ModalBottomSheet as a navigation destination using directOverlay().
 */
package dev.enro.recipes.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import dev.enro.ui.scenes.directOverlay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object BottomSheetRecipe : NavigationKey

@Serializable
data object SheetHost : NavigationKey

@Serializable
data class OptionsSheet(val title: String) : NavigationKey

@Serializable
data class DetailSheet(val itemId: String) : NavigationKey

@Composable
@NavigationDestination(BottomSheetRecipe::class)
fun BottomSheetRecipeScreen() {
    val navigation = navigationHandle<BottomSheetRecipe>()
    RecipeScaffold(
        title = "Bottom Sheet Navigation",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(SheetHost.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@NavigationDestination(OptionsSheet::class)
val optionsSheetDestination: NavigationDestinationProvider<OptionsSheet> = navigationDestination(
    metadata = {
        directOverlay()
    }
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = { navigation.requestClose() },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Detail for item: ${navigation.key.itemId}")
            Text("This sheet is fully expanded by default.")

            Button(onClick = {
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

@Composable
@NavigationDestination(SheetHost::class)
fun SheetHostDestination() {
    val navigation = navigationHandle<SheetHost>()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
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
