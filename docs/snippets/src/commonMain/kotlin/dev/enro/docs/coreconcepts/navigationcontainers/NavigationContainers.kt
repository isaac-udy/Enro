// Snippets for docs/ghpages/docs/core-concepts/navigation-containers.md
package dev.enro.docs.coreconcepts.navigationcontainers

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.enro.NavigationContainer
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

@Serializable
data object Home : NavigationKey

@Serializable
data object Profile : NavigationKey

@Serializable
data class ShowProfile(val userId: String) : NavigationKey

@Serializable
data object MainTabs : NavigationKey

enum class Tab { Home, Profile }

@Composable
private fun TabBar(current: Tab, onSelect: (Tab) -> Unit) {
}

@Composable
fun RootContent() {
    // region docs:navigation-containers/container
    val container = rememberNavigationContainer(
        backstack = backstackOf(Home.asInstance()),
    )
    NavigationDisplay(state = container)
    // endregion
}

fun buildBackstack() {
    // region docs:navigation-containers/backstack
    val backstack = backstackOf(
        Home.asInstance(),
        ShowProfile("user-123").asInstance(),
    )
    // endregion
}

// region docs:navigation-containers/nested
@Composable
@NavigationDestination(MainTabs::class)
fun MainTabsDestination() {
    var tab by remember { mutableStateOf(Tab.Home) }

    val homeContainer = rememberNavigationContainer(
        key = NavigationContainer.Key("home"),
        backstack = backstackOf(Home.asInstance()),
    )
    val profileContainer = rememberNavigationContainer(
        key = NavigationContainer.Key("profile"),
        backstack = backstackOf(Profile.asInstance()),
    )

    Column {
        when (tab) {
            Tab.Home -> NavigationDisplay(state = homeContainer)
            Tab.Profile -> NavigationDisplay(state = profileContainer)
        }
        TabBar(current = tab, onSelect = { tab = it })
    }
}
// endregion
