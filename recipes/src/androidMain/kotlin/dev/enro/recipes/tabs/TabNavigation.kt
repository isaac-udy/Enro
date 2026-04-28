/**
 * Enro Recipe: Tab Navigation
 *
 * Demonstrates tab-based navigation with NavigationContainerGroup.
 *
 * NavigationContainerGroup is provided by enro-compat and is currently Android-only,
 * so this recipe lives in androidMain.
 */
package dev.enro.recipes.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.NavigationContainer
import dev.enro.NavigationKey
import dev.enro.acceptNone
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.core.compose.container.NavigationContainerGroup
import dev.enro.core.compose.container.rememberNavigationContainerGroup
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.recipes.RecipeScaffold
import dev.enro.ui.EmptyBehavior
import dev.enro.ui.NavigationContainerState
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

@Serializable
data object HomeTab : NavigationKey

@Serializable
data object SearchTab : NavigationKey

@Serializable
data object ProfileTab : NavigationKey

@Serializable
data class HomeDetail(val id: String) : NavigationKey

@Serializable
data class SearchResult(val query: String) : NavigationKey

@Composable
@NavigationDestination(TabsRecipe::class)
fun TabsRecipeScreen() {
    val navigation = navigationHandle<TabsRecipe>()
    RecipeScaffold(
        title = "Tab Navigation",
        navigation = navigation,
    ) { modifier ->
        val homeContainer = rememberNavigationContainer(
            key = NavigationContainer.Key("home-tab"),
            backstack = backstackOf(HomeTab.asInstance()),
            filter = acceptNone(),
            emptyBehavior = EmptyBehavior.preventEmpty(),
        )

        val searchContainer = rememberNavigationContainer(
            key = NavigationContainer.Key("search-tab"),
            backstack = backstackOf(SearchTab.asInstance()),
            filter = acceptNone(),
            emptyBehavior = EmptyBehavior.preventEmpty(),
        )

        val profileContainer = rememberNavigationContainer(
            key = NavigationContainer.Key("profile-tab"),
            backstack = backstackOf(ProfileTab.asInstance()),
            filter = acceptNone(),
            emptyBehavior = EmptyBehavior.preventEmpty(),
        )

        val group = rememberNavigationContainerGroup(
            homeContainer,
            searchContainer,
            profileContainer,
        )

        Column(modifier = modifier) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                NavigationDisplay(state = group.activeContainer)
            }
            TabBar(group = group)
        }
    }
}

@Composable
private fun TabBar(group: NavigationContainerGroup) {
    data class TabItem(
        val container: NavigationContainerState,
        val label: String,
        val icon: @Composable () -> Unit,
    )

    val tabs = listOf(
        TabItem(group.containers[0], "Home") { Icon(Icons.Default.Home, "Home") },
        TabItem(group.containers[1], "Search") { Icon(Icons.Default.Search, "Search") },
        TabItem(group.containers[2], "Profile") { Icon(Icons.Default.Person, "Profile") },
    )

    NavigationBar {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = tab.container == group.activeContainer,
                onClick = { group.setActive(tab.container) },
                icon = tab.icon,
                label = { Text(tab.label) },
            )
        }
    }
}

@Composable
@NavigationDestination(HomeTab::class)
fun HomeTabDestination() {
    val navigation = navigationHandle<HomeTab>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Home Tab")
        Button(onClick = { navigation.open(HomeDetail("item-1")) }) {
            Text("View Item 1")
        }
        Button(onClick = { navigation.open(HomeDetail("item-2")) }) {
            Text("View Item 2")
        }
    }
}

@Composable
@NavigationDestination(SearchTab::class)
fun SearchTabDestination() {
    val navigation = navigationHandle<SearchTab>()
    var query by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Search Tab")
        TextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search query") },
        )
        Button(
            onClick = { navigation.open(SearchResult(query)) },
            enabled = query.isNotBlank(),
        ) {
            Text("Search")
        }
    }
}

@Composable
@NavigationDestination(ProfileTab::class)
fun ProfileTabDestination() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Profile Tab")
        Text("Your profile information here")
    }
}

@Composable
@NavigationDestination(HomeDetail::class)
fun HomeDetailDestination() {
    val navigation = navigationHandle<HomeDetail>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Home Detail: ${navigation.key.id}")
        Button(onClick = { navigation.close() }) {
            Text("Back")
        }
    }
}

@Composable
@NavigationDestination(SearchResult::class)
fun SearchResultDestination() {
    val navigation = navigationHandle<SearchResult>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Search results for: ${navigation.key.query}")
        Button(onClick = { navigation.close() }) {
            Text("Back")
        }
    }
}
