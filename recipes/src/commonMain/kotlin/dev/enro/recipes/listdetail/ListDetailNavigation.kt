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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.*
import dev.enro.annotations.NavigationDestination
import dev.enro.recipes.RecipeScaffold
import dev.enro.ui.EmptyBehavior
import dev.enro.ui.NavigationAnimations
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

@Serializable
object ListDetailRecipe : NavigationKey

@Serializable
data object ItemList : NavigationKey

@Serializable
data class ItemDetailScreen(val itemId: String, val title: String) : NavigationKey

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
        DualPaneLayout(modifier = modifier)
    }
}

@Composable
private fun DualPaneLayout(
    modifier: Modifier = Modifier,
) {
    val listContainer = rememberNavigationContainer(
        key = NavigationContainer.Key("list-pane"),
        backstack = backstackOf(ItemList.asInstance()),
        filter = acceptNone(),
    )

    val detailContainer = rememberNavigationContainer(
        key = NavigationContainer.Key("detail-pane"),
        backstack = backstackOf(),
        filter = accept { key<ItemDetailScreen>() },
        emptyBehavior = EmptyBehavior.allowEmpty(),
    )

    // Only keep the single top-most ItemDetailScreen in the detail container
    LaunchedEffect(detailContainer.backstack) {
        val detailCount = detailContainer.backstack.count { it.key is ItemDetailScreen }
        if (detailCount <= 1) {
            return@LaunchedEffect
        }
        detailContainer.updateBackstack { backstack ->
            val lastDetailIndex = detailContainer.backstack.indexOfLast { it.key is ItemDetailScreen }
            backstack.subList(lastDetailIndex, backstack.size).asBackstack()
        }
    }
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val showAsDualPane = maxWidth >= 600.dp
        val showList = showAsDualPane || detailContainer.backstack.isEmpty()
        val showDetail = showAsDualPane || detailContainer.backstack.isNotEmpty()

        val listWidth =  animateDpAsState(
            when {
                showAsDualPane -> maxWidth * 0.4f
                else -> maxWidth
            }
        ).value

        val detailWidth = animateDpAsState(
            when {
                showAsDualPane -> maxWidth * 0.6f
                else -> maxWidth
            }
        ).value

        val detailOffset = animateDpAsState(
            when {
                showAsDualPane -> maxWidth * 0.4f
                else -> 0.dp
            },
        ).value

        AnimatedVisibility(
            visible = showList,
            modifier = Modifier
                .width(listWidth)
                .fillMaxHeight(),
        ) {
            NavigationDisplay(
                state = listContainer
            )
        }

        AnimatedVisibility(
            visible = showDetail,
            modifier = Modifier
                .width(detailWidth)
                .offset(x = detailOffset)
                .fillMaxHeight(),
        ) {
            NavigationDisplay(
                state = detailContainer,
                animations = NavigationAnimations(
                    transitionSpec = NavigationAnimations.Default.containerTransitionSpec,
                    popTransitionSpec = NavigationAnimations.Default.containerTransitionSpec,
                    predictivePopTransitionSpec = NavigationAnimations.Default.containerTransitionSpec,
                )
            )
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
