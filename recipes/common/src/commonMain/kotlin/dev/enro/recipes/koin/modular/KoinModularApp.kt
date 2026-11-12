/**
 * Enro Recipe: Koin Modular
 *
 * Shows two features that each declare a Koin module and open the other
 * feature by NavigationKey. Each package represents a Gradle module:
 *
 *  - `koin.modular` (this file) is the app. The app declares the keys, the
 *    shared session, and the Koin application that loads the module of each
 *    feature.
 *  - `koin.modular.orders` and `koin.modular.account` are features. Neither
 *    feature imports the other feature.
 *
 * Koin provides dependencies only. `enro-processor` registers the
 * destinations, as in the Modular Navigation recipe, so the Koin module of a
 * feature declares no destinations.
 *
 * [KoinSession] is a `single`, so both features receive the same instance:
 * the orders screen shows the name that the account screen stores.
 *
 * Each destination creates a ViewModel with `koinEnroViewModel` from the
 * Koin ViewModel recipe.
 *
 * Nav3 equivalent: the Koin modular recipe.
 */
package dev.enro.recipes.koin.modular

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.navigationHandle
import dev.enro.recipes.RecipeScaffold
import dev.enro.recipes.koin.modular.account.accountModule
import dev.enro.recipes.koin.modular.orders.ordersModule
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable
import org.koin.compose.KoinIsolatedContext
import org.koin.dsl.koinApplication
import org.koin.dsl.module

@Serializable
object KoinModularRecipe : NavigationKey

// The app declares the keys, so each feature can open the other feature's
// key without importing the other feature.
@Serializable
data object KoinOrders : NavigationKey

@Serializable
data object KoinAccount : NavigationKey

class KoinSession {
    var userName: String by mutableStateOf("Guest")
}

// region docs:di/koin-modular-app
val appModule = module {
    // `single`: every feature receives the same KoinSession instance.
    single { KoinSession() }
}

// The Koin application loads the app module and the module of each feature.
private val recipeKoin = koinApplication {
    modules(appModule, ordersModule, accountModule)
}
// endregion

@Composable
@NavigationDestination(KoinModularRecipe::class)
fun KoinModularRecipeScreen() {
    val navigation = navigationHandle<KoinModularRecipe>()
    RecipeScaffold(
        title = "Koin Modular",
        navigation = navigation,
    ) { modifier ->
        // An app that calls startKoin at startup does not require KoinIsolatedContext.
        KoinIsolatedContext(context = recipeKoin) {
            val container = rememberNavigationContainer(
                backstack = backstackOf(KoinOrders.asInstance()),
            )
            NavigationDisplay(
                state = container,
                modifier = modifier,
            )
        }
    }
}
