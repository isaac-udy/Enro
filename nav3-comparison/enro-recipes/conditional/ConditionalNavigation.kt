/**
 * Enro Recipe: Conditional Navigation
 *
 * Nav3 equivalent: "Conditional Navigation" recipe
 * https://nicbell.github.io/nav3/recipes/conditional-navigation
 *
 * Demonstrates how Enro handles conditional navigation (e.g., authentication gates)
 * using NavigationInterceptors, compared to Nav3's approach.
 *
 * Key differences from Nav3:
 * - Nav3 suggests using a "conditional NavEntry" that checks a condition and either
 *   redirects or shows content. This is done inline in the entryProvider.
 * - Enro uses NavigationInterceptors, which intercept navigation operations before they
 *   reach the container. This is more powerful because:
 *   1. The interceptor runs BEFORE the destination is created or rendered.
 *   2. It can cancel the navigation, redirect, or replace the destination.
 *   3. It's reusable across the entire app (registered in a NavigationModule).
 *   4. Multiple interceptors can be composed.
 * - Enro interceptors are declarative and centralized, while Nav3's conditional
 *   navigation is scattered across individual entry providers.
 */
package dev.enro.recipes.conditional

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
import dev.enro.close
import dev.enro.controller.createNavigationModule
import dev.enro.interceptor.builder.navigationInterceptor
import dev.enro.navigationHandle
import dev.enro.open
import kotlinx.serialization.Serializable

// -- Navigation Keys --

@Serializable
data object PublicHome : NavigationKey

@Serializable
data object ProtectedDashboard : NavigationKey

@Serializable
data object ProtectedProfile : NavigationKey

@Serializable
data class LoginScreen(val redirectTo: NavigationKey? = null) : NavigationKey

@Serializable
data object PremiumContent : NavigationKey

@Serializable
data object UpgradeScreen : NavigationKey

// -- Auth State --
// In a real app, this would come from a repository or session manager.
object AuthState {
    var isLoggedIn: Boolean = false
    var isPremium: Boolean = false
}

// -- Navigation Interceptor --
// This is the core Enro pattern for conditional navigation.
// Nav3 has no direct equivalent -- you'd handle this per-destination.

val authInterceptor = navigationInterceptor {
    // Intercept any attempt to open a protected screen.
    // If the user is not logged in, redirect to login.
    onOpened<ProtectedDashboard> {
        if (!AuthState.isLoggedIn) {
            // Cancel this navigation and redirect to login.
            // After login, the user can navigate to the dashboard manually.
            cancelAnd {
                // Note: In a real app, you'd use the navigation handle from context
                // to open the login screen. This is simplified for the recipe.
            }
        } else {
            continueWithOpen()
        }
    }

    onOpened<ProtectedProfile> {
        if (!AuthState.isLoggedIn) {
            // Replace the navigation with login, passing the original destination
            // so we can redirect after successful login.
            replaceWith(LoginScreen(redirectTo = key))
        } else {
            continueWithOpen()
        }
    }

    // Premium content gate
    onOpened<PremiumContent> {
        if (!AuthState.isLoggedIn) {
            replaceWith(LoginScreen(redirectTo = key))
        } else if (!AuthState.isPremium) {
            replaceWith(UpgradeScreen)
        } else {
            continueWithOpen()
        }
    }
}

// Register the interceptor in a NavigationModule.
// This makes it apply globally to all containers in the app.
val conditionalNavigationModule = createNavigationModule {
    interceptor(authInterceptor)
}

// -- Destinations --

@Composable
@NavigationDestination(PublicHome::class)
fun PublicHomeDestination() {
    val navigation = navigationHandle<PublicHome>()

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Public Home")
        Text("Logged in: ${AuthState.isLoggedIn}")

        // These buttons trigger navigation that may be intercepted.
        // The destination code doesn't need to know about auth checks --
        // the interceptor handles it transparently.
        Button(onClick = { navigation.open(ProtectedDashboard) }) {
            Text("Go to Dashboard (protected)")
        }

        Button(onClick = { navigation.open(ProtectedProfile) }) {
            Text("Go to Profile (protected, with redirect)")
        }

        Button(onClick = { navigation.open(PremiumContent) }) {
            Text("Go to Premium Content (premium gate)")
        }

        Button(onClick = { navigation.open(LoginScreen()) }) {
            Text("Login")
        }
    }
}

@Composable
@NavigationDestination(LoginScreen::class)
fun LoginScreenDestination() {
    val navigation = navigationHandle<LoginScreen>()
    var username by rememberSaveable { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Login")

        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
        )

        Button(
            onClick = {
                AuthState.isLoggedIn = true
                // After login, redirect to the original destination if one was provided.
                val redirect = navigation.key.redirectTo
                if (redirect != null) {
                    navigation.open(redirect)
                } else {
                    navigation.close()
                }
            },
            enabled = username.isNotBlank(),
        ) {
            Text("Sign In")
        }
    }
}

@Composable
@NavigationDestination(ProtectedDashboard::class)
fun ProtectedDashboardDestination() {
    val navigation = navigationHandle<ProtectedDashboard>()
    Column {
        Text("Protected Dashboard")
        Text("Welcome! You are authenticated.")
        Button(onClick = { navigation.close() }) {
            Text("Back")
        }
    }
}

@Composable
@NavigationDestination(ProtectedProfile::class)
fun ProtectedProfileDestination() {
    val navigation = navigationHandle<ProtectedProfile>()
    Column {
        Text("Protected Profile")
        Text("Your profile information")
        Button(onClick = { navigation.close() }) {
            Text("Back")
        }
    }
}

@Composable
@NavigationDestination(PremiumContent::class)
fun PremiumContentDestination() {
    Column {
        Text("Premium Content")
        Text("Exclusive content for premium users")
    }
}

@Composable
@NavigationDestination(UpgradeScreen::class)
fun UpgradeScreenDestination() {
    val navigation = navigationHandle<UpgradeScreen>()
    Column {
        Text("Upgrade to Premium")
        Text("Get access to exclusive content")
        Button(onClick = {
            AuthState.isPremium = true
            navigation.open(PremiumContent)
        }) {
            Text("Upgrade Now")
        }
        Button(onClick = { navigation.close() }) {
            Text("Maybe Later")
        }
    }
}
