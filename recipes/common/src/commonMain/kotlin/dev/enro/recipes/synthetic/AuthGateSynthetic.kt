/**
 * Enro Recipe: Synthetic Destination — Auth gate
 *
 * Shows a conditional redirect with `syntheticDestination`. The synthetic
 * destination reads the auth state and calls `open(...)` with the protected
 * feature or with the login screen. The synthetic destination renders no UI.
 *
 * A caller opens the key of the synthetic destination and does not check the
 * auth state.
 *
 * The Conditional Navigation recipe (`dev.enro.recipes.conditional`) builds
 * the same gate with a NavigationInterceptor. With an interceptor, callers
 * open the key of the protected destination.
 */
package dev.enro.recipes.synthetic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.closeAndReplaceWith
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.recipes.RecipeScaffold
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.destinations.syntheticDestination
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

@Serializable
object AuthGateSyntheticRecipe : NavigationKey

@Serializable
object AuthGateHome : NavigationKey

@Serializable
object AuthGateLogin : NavigationKey

@Serializable
object AuthGateProtectedFeature : NavigationKey

/**
 * The key of the synthetic auth gate. A caller opens [RequireProtectedFeature]
 * in place of [AuthGateProtectedFeature].
 */
@Serializable
object RequireProtectedFeature : NavigationKey

// A top-level property in place of a session repository.
private var isLoggedIn: Boolean by mutableStateOf(false)

// The `open` outcome opens another key in place of the synthetic. The
// instance of RequireProtectedFeature is not added to the backstack.
@NavigationDestination(RequireProtectedFeature::class)
val requireProtectedFeature = syntheticDestination<RequireProtectedFeature> {
    if (isLoggedIn) {
        open(AuthGateProtectedFeature)
    } else {
        open(AuthGateLogin)
    }
}

@Composable
@NavigationDestination(AuthGateSyntheticRecipe::class)
fun AuthGateSyntheticRecipeScreen() {
    val navigation = navigationHandle<AuthGateSyntheticRecipe>()
    RecipeScaffold(
        title = "Synthetic: Auth gate",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(AuthGateHome.asInstance()),
        )
        NavigationDisplay(state = container, modifier = modifier)
    }
}

@Composable
@NavigationDestination(AuthGateHome::class)
fun AuthGateHomeDestination() {
    val navigation = navigationHandle<AuthGateHome>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Auth gate via a synthetic destination",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "Tapping the button below opens `RequireProtectedFeature`. The synthetic " +
                "checks the auth state and either forwards to the protected screen or " +
                "redirects to the login screen.",
            style = MaterialTheme.typography.bodySmall,
        )

        Row {
            Text(
                text = "Currently logged in: $isLoggedIn",
                modifier = Modifier.padding(end = 12.dp),
            )
            Switch(
                checked = isLoggedIn,
                onCheckedChange = { isLoggedIn = it },
            )
        }
        Text(
            text = "(In a real app this would be backed by a session repository. The " +
                "switch is here so you can toggle the state without going through the " +
                "login flow.)",
            style = MaterialTheme.typography.bodySmall,
        )

        Button(onClick = { navigation.open(RequireProtectedFeature) }) {
            Text("Open protected feature (via synthetic)")
        }
    }
}

@Composable
@NavigationDestination(AuthGateLogin::class)
fun AuthGateLoginDestination() {
    val navigation = navigationHandle<AuthGateLogin>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = "Login screen", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "You were redirected here by the synthetic because you weren't logged in. " +
                "Tap the button below to 'log in' and continue to the protected feature.",
            style = MaterialTheme.typography.bodySmall,
        )
        Button(
            onClick = {
                isLoggedIn = true
                navigation.closeAndReplaceWith(AuthGateProtectedFeature)
            },
        ) {
            Text("Log in")
        }
        Button(onClick = { navigation.close() }) {
            Text("Cancel")
        }
    }
}

@Composable
@NavigationDestination(AuthGateProtectedFeature::class)
fun AuthGateProtectedFeatureDestination() {
    val navigation = navigationHandle<AuthGateProtectedFeature>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = "Protected feature", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "You reached this screen because the auth gate let you through. The " +
                "synthetic checked `isLoggedIn` and forwarded straight to here.",
            style = MaterialTheme.typography.bodySmall,
        )
        Button(
            onClick = {
                isLoggedIn = false
                navigation.close()
            },
        ) {
            Text("Log out and go back")
        }
    }
}
