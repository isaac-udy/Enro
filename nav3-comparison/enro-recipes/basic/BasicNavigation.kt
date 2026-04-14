/**
 * Enro Recipe: Basic Navigation
 *
 * Nav3 equivalent: "Basic" recipe
 * https://nicbell.github.io/nav3/recipes/basic
 *
 * Demonstrates simple forward/back navigation between screens using Enro's
 * NavigationKey, @NavigationDestination, and NavigationHandle.
 *
 * Key differences from Nav3:
 * - Nav3 requires you to manage a mutable backstack list and manually add/remove entries.
 * - Enro manages the backstack internally; you just call navigation.open() and navigation.close().
 * - Nav3 requires NavDisplay + NavEntry DSL to render screens; Enro uses NavigationDisplay + annotated destinations.
 * - Enro's @NavigationDestination annotation automatically binds keys to composables via KSP code generation.
 */
package dev.enro.recipes.basic

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

// -- Navigation Keys --
// In Nav3, you define keys similarly but without Serializable support out of the box.
// Enro uses @Serializable for automatic state saving and deep link support.

@Serializable
data object HomeScreen : NavigationKey

@Serializable
data class ProfileScreen(val userId: String) : NavigationKey

@Serializable
data class SettingsScreen(val section: String = "general") : NavigationKey

// -- Host Screen --
// Nav3 equivalent: The composable that holds NavDisplay and the backstack.
// In Nav3, you would write:
//   val backStack = rememberNavBackStack(HomeScreen)
//   NavDisplay(backStack) { entry -> when(entry) { ... } }
//
// In Enro, the container manages the backstack, and destinations are registered
// via annotations rather than inline lambdas.

@Composable
fun AppHost() {
    // rememberNavigationContainer automatically saves/restores the backstack across
    // configuration changes and process death. Nav3 requires rememberNavBackStack for this.
    val container = rememberNavigationContainer(
        backstack = backstackOf(HomeScreen.asInstance()),
    )

    Scaffold { padding ->
        // NavigationDisplay renders the active destination from the container's backstack.
        // It handles animations, predictive back gestures, and scene management automatically.
        NavigationDisplay(
            state = container,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        )
    }
}

// -- Destinations --
// In Nav3, destinations are defined inline within NavDisplay's content lambda.
// In Enro, they are standalone composables annotated with @NavigationDestination,
// which are discovered at compile time via KSP. This means destinations can live
// in different modules without the host needing to know about them.

@Composable
@NavigationDestination(HomeScreen::class)
fun HomeScreenDestination() {
    // navigationHandle() provides access to the current key and navigation operations.
    // Nav3 equivalent: the key is passed directly as a parameter to the content lambda.
    val navigation = navigationHandle<HomeScreen>()

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Home Screen")

        // In Nav3: backStack.add(ProfileScreen("user-123"))
        // In Enro: navigation.open(...) finds the appropriate container and adds to its backstack.
        Button(onClick = { navigation.open(ProfileScreen("user-123")) }) {
            Text("View Profile")
        }

        Button(onClick = { navigation.open(SettingsScreen()) }) {
            Text("Settings")
        }
    }
}

@Composable
@NavigationDestination(ProfileScreen::class)
fun ProfileScreenDestination() {
    val navigation = navigationHandle<ProfileScreen>()
    // Access the key's properties directly - type-safe access to navigation arguments.
    val userId = navigation.key.userId

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Profile for user: $userId")

        Button(onClick = { navigation.open(SettingsScreen("profile")) }) {
            Text("User Settings")
        }

        // In Nav3: backStack.removeLastOrNull()
        // In Enro: navigation.close() removes this screen from its parent container.
        Button(onClick = { navigation.close() }) {
            Text("Go Back")
        }
    }
}

@Composable
@NavigationDestination(SettingsScreen::class)
fun SettingsScreenDestination() {
    val navigation = navigationHandle<SettingsScreen>()
    val section = navigation.key.section

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Settings: $section")

        Button(onClick = { navigation.close() }) {
            Text("Go Back")
        }
    }
}
