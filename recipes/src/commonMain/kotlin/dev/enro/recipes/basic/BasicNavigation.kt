/**
 * Enro Recipe: Basic Navigation
 *
 * Demonstrates simple forward/back navigation between screens.
 */
package dev.enro.recipes.basic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

// -- Root recipe key --

@Serializable
object BasicRecipe : NavigationKey

// -- Internal navigation keys --

@Serializable
data object BasicHomeScreen : NavigationKey

@Serializable
data class BasicProfileScreen(val userId: String) : NavigationKey

@Serializable
data class BasicSettingsScreen(val section: String = "general") : NavigationKey

@Composable
@NavigationDestination(BasicRecipe::class)
fun BasicRecipeScreen() {
    val navigation = navigationHandle<BasicRecipe>()
    RecipeScaffold(
        title = "Basic Navigation",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(BasicHomeScreen.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
        )
    }
}

@Composable
@NavigationDestination(BasicHomeScreen::class)
fun BasicHomeScreenDestination() {
    val navigation = navigationHandle<BasicHomeScreen>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Home Screen")
        Button(onClick = { navigation.open(BasicProfileScreen("user-123")) }) {
            Text("View Profile")
        }
        Button(onClick = { navigation.open(BasicSettingsScreen()) }) {
            Text("Settings")
        }
    }
}

@Composable
@NavigationDestination(BasicProfileScreen::class)
fun BasicProfileScreenDestination() {
    val navigation = navigationHandle<BasicProfileScreen>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Profile for user: ${navigation.key.userId}")
        Button(onClick = { navigation.open(BasicSettingsScreen("profile")) }) {
            Text("User Settings")
        }
        Button(onClick = { navigation.close() }) {
            Text("Go Back")
        }
    }
}

@Composable
@NavigationDestination(BasicSettingsScreen::class)
fun BasicSettingsScreenDestination() {
    val navigation = navigationHandle<BasicSettingsScreen>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Settings: ${navigation.key.section}")
        Button(onClick = { navigation.close() }) {
            Text("Go Back")
        }
    }
}
