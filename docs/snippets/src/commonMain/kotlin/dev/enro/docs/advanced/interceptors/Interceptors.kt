// Snippets for docs/ghpages/docs/advanced/interceptors.md
package dev.enro.docs.advanced.interceptors

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationComponent
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.controller.NavigationComponentConfiguration
import dev.enro.controller.createNavigationModule
import dev.enro.interceptor.builder.navigationInterceptor
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

@Serializable
data object Home : NavigationKey

@Serializable
data object Account : NavigationKey

@Serializable
data class Login(val redirectTo: NavigationKey? = null) : NavigationKey

@Composable
@NavigationDestination(Home::class)
fun HomeScreen() = Text("Home")

@Composable
@NavigationDestination(Account::class)
fun AccountScreen() = Text("Account")

@Composable
@NavigationDestination(Login::class)
fun LoginScreen() = Text("Login")

object Session {
    var isLoggedIn: Boolean = false
}

// region docs:interceptors/redirect
val authInterceptor = navigationInterceptor {
    onOpened<Account> {
        if (!Session.isLoggedIn) {
            // replaceWith does not return, so continueWithOpen() runs only when logged in.
            replaceWith(Login(redirectTo = key))
        }
        continueWithOpen()
    }
}
// endregion

// region docs:interceptors/on-component
@NavigationComponent
object MyComponent : NavigationComponentConfiguration(
    module = createNavigationModule {
        interceptor(authInterceptor)
    }
)
// endregion

@Composable
fun ContainerInterceptorExample() {
    // region docs:interceptors/on-container
    val container = rememberNavigationContainer(
        backstack = backstackOf(Home.asInstance()),
        interceptor = authInterceptor,
    )
    NavigationDisplay(state = container)
    // endregion
}
