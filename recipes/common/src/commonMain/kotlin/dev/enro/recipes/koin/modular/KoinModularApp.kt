/**
 * Enro Recipe: Koin Modular
 *
 * Demonstrates features that each provide a Koin module and navigate to each
 * other by key. The packages stand in for Gradle modules:
 *
 *  - `koin.modular` (this file) is the app. It declares the keys, the shared
 *    session, and the Koin application that loads every feature's module.
 *  - `koin.modular.orders` and `koin.modular.account` are features. Neither
 *    refers to the other.
 *
 * Koin only provides dependencies. The destinations are registered by the
 * `enro-processor`, as in the Modular Navigation recipe, so a feature's Koin
 * module has no navigation entries in it.
 *
 * [KoinSession] is a `single`, so both features get one instance: a name
 * saved on the account screen is shown on the orders screen.
 *
 * ViewModels are created with `koinEnroViewModel` from the Koin ViewModel
 * recipe.
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

// The keys both features use.
@Serializable
data object KoinOrders : NavigationKey

@Serializable
data object KoinAccount : NavigationKey

class KoinSession {
    var userName: String by mutableStateOf("Guest")
}

// region docs:di/koin-modular-app
val appModule = module {
    // One instance for every feature.
    single { KoinSession() }
}

// The app loads its own module and one module per feature.
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
        // A real app calls startKoin once at startup instead.
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
