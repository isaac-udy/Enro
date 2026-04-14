/**
 * Enro Recipe: Tab Navigation
 *
 * Nav3 equivalent: "Common Navigation UI" recipe
 * https://nicbell.github.io/nav3/recipes/common-nav-ui
 *
 * Demonstrates how to implement tab-based navigation with a bottom navigation bar in Enro,
 * using NavigationContainerGroup.
 *
 * Key differences from Nav3:
 * - Nav3 has no built-in tab abstraction. You manage multiple backstacks manually and
 *   conditionally render NavDisplay instances.
 * - Enro provides NavigationContainerGroup, which manages multiple NavigationContainers
 *   as a group with one active container at a time.
 * - NavigationContainerGroup automatically saves and restores the active tab across
 *   configuration changes and process death.
 * - Each tab has its own independent backstack and saved state, preserved when switching tabs.
 */
package dev.enro.recipes.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import dev.enro.ui.EmptyBehavior
import dev.enro.ui.NavigationContainerState
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

// -- Navigation Keys --

@Serializable
data object TabHost : NavigationKey

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

// -- Tab Host --
// Nav3 equivalent: Managing multiple backstack lists and switching between them.
// In Nav3, you'd have something like:
//   val homeBackStack = rememberNavBackStack(HomeTab)
//   val searchBackStack = rememberNavBackStack(SearchTab)
//   val profileBackStack = rememberNavBackStack(ProfileTab)
//   // Then conditionally show the right NavDisplay based on selected tab
//
// Enro simplifies this with NavigationContainerGroup.

@Composable
@NavigationDestination(TabHost::class)
fun TabHostDestination() {
    // Create a container for each tab.
    // Each has its own backstack and saved state.
    // acceptNone() means the tab container only shows what's in its initial backstack
    // and screens opened from within it -- it won't capture random navigations.
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

    // NavigationContainerGroup manages which container is active.
    // It automatically saves/restores the active tab.
    // Nav3 has no equivalent -- you'd track selected tab with rememberSaveable.
    val group = rememberNavigationContainerGroup(
        homeContainer,
        searchContainer,
        profileContainer,
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Active tab content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            // Only render the active container's display.
            // The inactive containers keep their backstack and state in memory.
            NavigationDisplay(state = group.activeContainer)
        }

        // Bottom navigation bar
        TabBar(group = group)
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
                onClick = {
                    // setActive switches the visible container.
                    // The previous tab's backstack and state are preserved.
                    group.setActive(tab.container)
                },
                icon = tab.icon,
                label = { Text(tab.label) },
            )
        }
    }
}

// -- Tab Destinations --

@Composable
@NavigationDestination(HomeTab::class)
fun HomeTabDestination() {
    val navigation = navigationHandle<HomeTab>()
    Column {
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
    // This text field state is preserved when switching tabs and coming back.
    var query by rememberSaveable { mutableStateOf("") }

    Column {
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
    Column {
        Text("Profile Tab")
        Text("Your profile information here")
    }
}

@Composable
@NavigationDestination(HomeDetail::class)
fun HomeDetailDestination() {
    val navigation = navigationHandle<HomeDetail>()
    Column {
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
    Column {
        Text("Search results for: ${navigation.key.query}")
        Button(onClick = { navigation.close() }) {
            Text("Back")
        }
    }
}
