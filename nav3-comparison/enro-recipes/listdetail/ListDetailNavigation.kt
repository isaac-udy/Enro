/**
 * Enro Recipe: List-Detail Navigation
 *
 * Nav3 equivalent: "List-Detail Scene" recipe
 * https://nicbell.github.io/nav3/recipes/list-detail
 *
 * Demonstrates how to implement a list-detail (master-detail) layout in Enro.
 *
 * Key differences from Nav3:
 * - Nav3 provides a built-in ListDetailScene that splits the backstack into a list pane
 *   and a detail pane based on a predicate and window size.
 * - Enro does not have a built-in ListDetailScene, but you can implement this pattern
 *   using a custom NavigationSceneStrategy or by using two NavigationContainers side by side.
 * - The two-container approach is more explicit and gives full control over layout behavior.
 * - For adaptive layouts (phone vs tablet), you can switch between single-container and
 *   dual-container layouts based on window size class.
 *
 * This recipe shows the two-container approach, which is the most straightforward in Enro.
 */
package dev.enro.recipes.listdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowWidthSizeClass
import dev.enro.NavigationContainer
import dev.enro.NavigationKey
import dev.enro.accept
import dev.enro.acceptNone
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.ui.EmptyBehavior
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

// -- Navigation Keys --

@Serializable
data object ListDetailHost : NavigationKey

@Serializable
data object ItemList : NavigationKey

@Serializable
data class ItemDetailScreen(val itemId: String, val title: String) : NavigationKey

@Serializable
data object EmptyDetailPlaceholder : NavigationKey

// -- Sample data --
data class Item(val id: String, val title: String, val description: String)

val sampleItems = listOf(
    Item("1", "First Item", "Description of the first item"),
    Item("2", "Second Item", "Description of the second item"),
    Item("3", "Third Item", "Description of the third item"),
)

// -- List-Detail Host --
// This host manages the layout, switching between single-pane (phone) and dual-pane (tablet).

@Composable
@NavigationDestination(ListDetailHost::class)
fun ListDetailHostDestination() {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED) {
        // Dual-pane layout for tablets/large screens
        DualPaneLayout()
    } else {
        // Single-pane layout for phones
        SinglePaneLayout()
    }
}

@Composable
private fun SinglePaneLayout() {
    // In single-pane mode, list and detail share one container.
    // When a detail is opened, it pushes onto the backstack and replaces the list.
    // Nav3's ListDetailScene falls back to single-pane automatically on small screens.
    val container = rememberNavigationContainer(
        backstack = backstackOf(ItemList.asInstance()),
    )
    NavigationDisplay(state = container)
}

@Composable
private fun DualPaneLayout() {
    // In dual-pane mode, we use two containers side by side.
    // The list container always shows the list.
    // The detail container shows the selected item detail.
    // Nav3's ListDetailScene handles this split via a predicate function.

    val listContainer = rememberNavigationContainer(
        key = NavigationContainer.Key("list-pane"),
        backstack = backstackOf(ItemList.asInstance()),
        filter = acceptNone(), // list pane does not accept new navigation
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

// -- Destinations --

@Composable
@NavigationDestination(ItemList::class)
fun ItemListDestination() {
    val navigation = navigationHandle<ItemList>()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(sampleItems) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable {
                        // navigation.open() will find the appropriate container.
                        // In single-pane: pushes onto the same container.
                        // In dual-pane: the detail container accepts ItemDetailScreen
                        // keys via its filter, so it receives this navigation.
                        navigation.open(ItemDetailScreen(item.id, item.title))
                    },
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(
            text = "Select an item to view details",
            modifier = Modifier.padding(16.dp),
        )
    }
}
