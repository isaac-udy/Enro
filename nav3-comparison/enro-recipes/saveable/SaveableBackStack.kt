/**
 * Enro Recipe: Saveable Back Stack
 *
 * Nav3 equivalent: "Saveable Back Stack" recipe
 * https://nicbell.github.io/nav3/recipes/saveable
 *
 * Demonstrates how Enro handles state saving automatically, compared to Nav3's
 * explicit rememberNavBackStack requirement.
 *
 * Key differences from Nav3:
 * - Nav3 requires `rememberNavBackStack` to make the backstack survive config changes and process death.
 *   Without it, backstack state is lost.
 * - Enro's `rememberNavigationContainer` automatically saves and restores the entire backstack
 *   using kotlinx.serialization. NavigationKeys must be @Serializable (which they should be anyway).
 * - rememberSaveable within destinations works identically in both frameworks since both use
 *   Compose's SaveableStateHolder under the hood.
 * - Enro preserves per-destination saved state even when a destination is not visible (e.g., behind
 *   another screen on the backstack), just like Nav3 with rememberNavBackStack.
 */
package dev.enro.recipes.saveable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
// All NavigationKeys are @Serializable, enabling automatic backstack persistence.
// Nav3 uses Parcelable or custom serialization; Enro uses kotlinx.serialization.

@Serializable
data object FormScreen : NavigationKey

@Serializable
data object ReviewScreen : NavigationKey

// -- Host --

@Composable
fun SaveableApp() {
    // This single call handles everything Nav3 does with rememberNavBackStack:
    // - Survives configuration changes (rotation, dark mode, etc.)
    // - Survives process death and restoration
    // - Each NavigationKey.Instance has a stable ID for SaveableStateHolder keying
    //
    // Nav3 equivalent:
    //   val backStack = rememberNavBackStack(FormScreen)
    //   NavDisplay(backStack, ...) { ... }
    val container = rememberNavigationContainer(
        backstack = backstackOf(FormScreen.asInstance()),
    )
    NavigationDisplay(state = container)
}

// -- Destinations --

@Composable
@NavigationDestination(FormScreen::class)
fun FormScreenDestination() {
    val navigation = navigationHandle<FormScreen>()

    // rememberSaveable works exactly the same as in Nav3.
    // The saved state is scoped to this destination instance and survives:
    // - Being moved behind another screen on the backstack
    // - Configuration changes
    // - Process death
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Fill out the form")

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
        )

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
        )

        Button(
            onClick = { navigation.open(ReviewScreen) },
            enabled = name.isNotBlank() && email.isNotBlank(),
        ) {
            Text("Review")
        }

        // When the user navigates to ReviewScreen and then comes back,
        // the name and email fields will still have their values.
        // This is automatic -- no additional setup required.
    }
}

@Composable
@NavigationDestination(ReviewScreen::class)
fun ReviewScreenDestination() {
    val navigation = navigationHandle<ReviewScreen>()

    // This counter survives config changes and process death.
    var tapCount by rememberSaveable { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Review your submission")
        Text("You've tapped the button $tapCount times")

        Button(onClick = { tapCount++ }) {
            Text("Tap me")
        }

        Button(onClick = { navigation.close() }) {
            Text("Go Back to Form")
        }
    }
}
