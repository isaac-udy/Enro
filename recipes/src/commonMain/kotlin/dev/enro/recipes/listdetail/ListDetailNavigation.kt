/**
 * Enro Recipe: List-Detail Navigation
 *
 * Demonstrates a list-detail (master-detail) layout using two NavigationContainers.
 *
 * Note: This recipe uses a simple BoxWithConstraints-based width threshold to choose
 * between single-pane and dual-pane. In a real app you would use Material3's
 * windowSizeClass APIs (compose-material3-adaptive) for a proper Window Size Class.
 */
package dev.enro.recipes.listdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.NavigationContainer
import dev.enro.NavigationKey
import dev.enro.accept
import dev.enro.acceptNone
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.recipes.RecipeScaffold
import dev.enro.ui.EmptyBehavior
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

@Serializable
object ListDetailRecipe : NavigationKey

@Serializable
data object ItemList : NavigationKey

@Serializable
data class ItemDetailScreen(val itemId: String, val title: String) : NavigationKey

@Serializable
data object EmptyDetailPlaceholder : NavigationKey

data class ListItem(val id: String, val title: String, val description: String)

internal val sampleItems = listOf(
    ListItem("1", "First Item", "Description of the first item"),
    ListItem("2", "Second Item", "Description of the second item"),
    ListItem("3", "Third Item", "Description of the third item"),
)

@Composable
@NavigationDestination(ListDetailRecipe::class)
fun ListDetailRecipeScreen() {
    val navigation = navigationHandle<ListDetailRecipe>()
    RecipeScaffold(
        title = "List-Detail Navigation",
        navigation = navigation,
    ) { modifier ->
        BoxWithConstraints(modifier = modifier) {
            if (maxWidth >= 600.dp) {
                DualPaneLayout()
            } else {
                SinglePaneLayout()
            }
        }
    }
}

@Composable
private fun SinglePaneLayout() {
    val container = rememberNavigationContainer(
        backstack = backstackOf(ItemList.asInstance()),
    )
    NavigationDisplay(state = container)
}

@Composable
private fun DualPaneLayout() {
    val listContainer = rememberNavigationContainer(
        key = NavigationContainer.Key("list-pane"),
        backstack = backstackOf(ItemList.asInstance()),
        filter = acceptNone(),
    )

    val detailContainer = rememberNavigationContainer(
        key = NavigationContainer.Key("detail-pane"),
        backstack = backstackOf(EmptyDetailPlaceholder.asInstance()),
        filter = accept { key<ItemDetailScreen>() },
        emptyBehavior = EmptyBehavior.preventEmpty(),
    )

    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.4f),
        ) {
            NavigationDisplay(state = listContainer)
        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.6f),
        ) {
            NavigationDisplay(state = detailContainer)
        }
    }
}

@Composable
@NavigationDestination(ItemList::class)
fun ItemListDestination() {
    val navigation = navigationHandle<ItemList>()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        items(sampleItems) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable {
                        navigation.open(ItemDetailScreen(item.id, item.title))
                    },
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(item.title)
                    Text(item.description)
                }
            }
        }
    }
}

@Composable
@NavigationDestination(ItemDetailScreen::class)
fun ItemDetailScreenDestination() {
    val navigation = navigationHandle<ItemDetailScreen>()
    val item = sampleItems.find { it.id == navigation.key.itemId }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (item != null) {
            Text(item.title)
            Text(item.description)
        } else {
            Text("Item not found: ${navigation.key.itemId}")
        }
    }
}

@Composable
@NavigationDestination(EmptyDetailPlaceholder::class)
fun EmptyDetailPlaceholderDestination() {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Select an item to view details",
            modifier = Modifier.padding(16.dp),
        )
    }
}
