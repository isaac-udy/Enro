/**
 * Enro Recipe: Conditional Navigation
 *
 * Demonstrates conditional navigation (auth gates) using NavigationInterceptors.
 *
 * Note: For simplicity in this recipe app, the interceptor is created on the recipe
 * host using `interceptor` parameter on rememberNavigationContainer. In a real app
 * you would register interceptors via a NavigationModule on the controller.
 */
package dev.enro.recipes.conditional

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.interceptor.builder.navigationInterceptor
import dev.enro.navigationHandle
import dev.enro.open
import dev.enro.recipes.RecipeScaffold
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

@Serializable
object ConditionalRecipe : NavigationKey

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

object AuthState {
    var isLoggedIn: Boolean by mutableStateOf(false)
    var isPremium: Boolean by mutableStateOf(false)
}

private val authInterceptor = navigationInterceptor {
    onOpened<ProtectedDashboard> {
        if (!AuthState.isLoggedIn) {
            replaceWith(LoginScreen())
        } else {
            continueWithOpen()
        }
    }

    onOpened<ProtectedProfile> {
        if (!AuthState.isLoggedIn) {
            replaceWith(LoginScreen(redirectTo = key))
        } else {
            continueWithOpen()
        }
    }

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

@Composable
@NavigationDestination(ConditionalRecipe::class)
fun ConditionalRecipeScreen() {
    val navigation = navigationHandle<ConditionalRecipe>()
    RecipeScaffold(
        title = "Conditional Navigation",
        navigation = navigation,
    ) { modifier ->
        val container = rememberNavigationContainer(
            backstack = backstackOf(PublicHome.asInstance()),
            interceptor = authInterceptor,
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
        )
    }
}

@Composable
@NavigationDestination(PublicHome::class)
fun PublicHomeDestination() {
    val navigation = navigationHandle<PublicHome>()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Public Home")
        Text("Logged in: ${AuthState.isLoggedIn}")
        Text("Premium: ${AuthState.isPremium}")

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

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Login")

        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
        )

        Button(
            onClick = {
                AuthState.isLoggedIn = true
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
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
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
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
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
    val navigation = navigationHandle<PremiumContent>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Premium Content")
        Text("Exclusive content for premium users")
        Button(onClick = { navigation.close() }) {
            Text("Back")
        }
    }
}

@Composable
@NavigationDestination(UpgradeScreen::class)
fun UpgradeScreenDestination() {
    val navigation = navigationHandle<UpgradeScreen>()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
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
