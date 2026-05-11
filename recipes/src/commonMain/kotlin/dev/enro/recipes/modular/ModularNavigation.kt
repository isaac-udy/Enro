/**
 * Enro Recipe: Modular Navigation
 *
 * Demonstrates how Enro handles navigation across feature modules using
 * KSP code generation, without requiring a DI framework.
 *
 * In a real app, the keys would live in a shared :core:navigation module while
 * destinations would live in :feature:home, :feature:profile, etc. Each module
 * generates a NavigationModule via KSP. The application module composes them.
 *
 * For demonstration, this single file contains keys + multiple destinations that
 * would typically live in different modules.
 */
package dev.enro.recipes.modular

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.navigationDestination
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

@Serializable
object ModularNavigationRecipe : NavigationKey

// :core:navigation - Shared NavigationKey definitions
@Serializable
data object HomeKey : NavigationKey

@Serializable
data class ProfileKey(val userId: String) : NavigationKey

@Serializable
data class ModularSettingsKey(val section: String = "general") : NavigationKey

@Serializable
data object LoginKey : NavigationKey

@Composable
@NavigationDestination(ModularNavigationRecipe::class)
fun ModularNavigationRecipeScreen() {
    val navigation = navigationHandle<ModularNavigationRecipe>()
    RecipeScaffold(
        title = "Modular Navigation",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(HomeKey.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
        )
    }
}

// :feature:home - Home destination
@Composable
@NavigationDestination(HomeKey::class)
fun HomeScreen() {
    val navigation = navigationHandle<HomeKey>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Home Screen", style = MaterialTheme.typography.titleLarge)
        Text("(would live in :feature:home)", style = MaterialTheme.typography.bodySmall)

        Button(onClick = { navigation.open(ProfileKey("user-123")) }) {
            Text("View Profile")
        }
        Button(onClick = { navigation.open(ModularSettingsKey()) }) {
            Text("Settings")
        }
        Button(onClick = { navigation.open(LoginKey) }) {
            Text("Login (manually-registered destination)")
        }
    }
}

// :feature:profile - Profile destination
@Composable
@NavigationDestination(ProfileKey::class)
fun ProfileScreen() {
    val navigation = navigationHandle<ProfileKey>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Profile: ${navigation.key.userId}", style = MaterialTheme.typography.titleLarge)
        Text("(would live in :feature:profile)", style = MaterialTheme.typography.bodySmall)
        Button(onClick = { navigation.close() }) { Text("Back") }
    }
}

// :feature:settings - Settings destination
@Composable
@NavigationDestination(ModularSettingsKey::class)
fun SettingsScreen() {
    val navigation = navigationHandle<ModularSettingsKey>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Settings: ${navigation.key.section}", style = MaterialTheme.typography.titleLarge)
        Text("(would live in :feature:settings)", style = MaterialTheme.typography.bodySmall)
        Button(onClick = { navigation.close() }) { Text("Back") }
    }
}

// :feature:auth - Manually-registered destination via navigationDestination
@NavigationDestination(LoginKey::class)
val loginDestination: NavigationDestinationProvider<LoginKey> = navigationDestination {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Login Screen", style = MaterialTheme.typography.titleLarge)
        Text(
            "(Registered programmatically with navigationDestination)",
            style = MaterialTheme.typography.bodySmall,
        )
        Button(onClick = { navigation.close() }) { Text("Back") }
    }
}
