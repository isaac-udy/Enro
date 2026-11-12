/**
 * Enro Recipe: List-Detail with Containers
 *
 * Shows a list-detail layout with two NavigationContainers. The list
 * container has the filter `acceptNone()` and contains only the list. The
 * detail container has a filter that accepts `WithContainersItemDetail` keys.
 *
 * The List-Detail (Scene) recipe (`ListDetailNavigation.kt`) builds the same
 * layout with one backstack and a `NavigationSceneStrategy`, and is the
 * recommended approach. Use two containers when each pane requires a
 * separate backstack and filter, for example when the two panes host
 * different feature flows.
 *
 * The recipe selects one or two panes with a `BoxWithConstraints` width
 * threshold. Material 3 Adaptive provides `WindowSizeClass` for the same
 * decision.
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
object ListDetailWithContainersRecipe : NavigationKey

@Serializable
data object WithContainersItemList : NavigationKey

@Serializable
data class WithContainersItemDetail(val itemId: String, val title: String) : NavigationKey

private data class ListItem(val id: String, val title: String, val description: String)

private val sampleItems = listOf(
    ListItem("1", "First Item", "Description of the first item"),
    ListItem("2", "Second Item", "Description of the second item"),
    ListItem("3", "Third Item", "Description of the third item"),
)

@Composable
@NavigationDestination(ListDetailWithContainersRecipe::class)
fun ListDetailWithContainersRecipeScreen() {
    val navigation = navigationHandle<ListDetailWithContainersRecipe>()
    RecipeScaffold(
        title = "List-Detail (Containers)",
        navigation = navigation,
    ) { modifier ->
        DualPaneLayout(modifier = modifier)
    }
}

@Composable
private fun DualPaneLayout(
    modifier: Modifier = Modifier,
) {
    // acceptNone(): `open` adds no key to the list container.
    val listContainer = rememberNavigationContainer(
        key = NavigationContainer.Key("list-pane"),
        backstack = backstackOf(WithContainersItemList.asInstance()),
        filter = acceptNone(),
    )

    // The filter sends each WithContainersItemDetail to the detail container.
    // allowEmpty() lets Back close the last detail and leave the container empty.
    val detailContainer = rememberNavigationContainer(
        key = NavigationContainer.Key("detail-pane"),
        backstack = backstackOf(),
        filter = accept { key<WithContainersItemDetail>() },
        emptyBehavior = EmptyBehavior.allowEmpty(),
    )

    // Each open adds a WithContainersItemDetail to the detail container. This
    // effect removes every entry below the last WithContainersItemDetail.
    LaunchedEffect(detailContainer.backstack) {
        val detailCount = detailContainer.backstack.count { it.key is WithContainersItemDetail }
        if (detailCount <= 1) {
            return@LaunchedEffect
        }
        detailContainer.updateBackstack { backstack ->
            val lastDetailIndex = detailContainer.backstack.indexOfLast { it.key is WithContainersItemDetail }
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
                    predictivePopTransitionSpec = { _ ->
                        NavigationAnimations.Default.containerTransitionSpec(this)
                    },
                )
            )
        }
    }
}

@Composable
@NavigationDestination(WithContainersItemList::class)
fun WithContainersItemListDestination() {
    val navigation = navigationHandle<WithContainersItemList>()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        items(sampleItems) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable {
                        navigation.open(WithContainersItemDetail(item.id, item.title))
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
@NavigationDestination(WithContainersItemDetail::class)
fun WithContainersItemDetailDestination() {
    val navigation = navigationHandle<WithContainersItemDetail>()
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
