/**
 * Enro Recipes: Metro ViewModel and Metro Modular
 *
 * The host screens for the two Metro recipes. The recipes are in the
 * `:recipes:metro` module, which applies the Metro compiler plugin:
 *
 *  - `MetroViewModel.kt`: a ViewModel with dependencies from a Metro graph
 *    and arguments from its NavigationKey.
 *  - `modular/MetroModularApp.kt`: features that contribute to one Metro
 *    graph and open each other's screens by key.
 */
package dev.enro.recipes.metro

import androidx.compose.runtime.Composable
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.navigationHandle
import dev.enro.recipes.RecipeScaffold
import dev.enro.recipes.metro.modular.MetroModularRecipeContent
import kotlinx.serialization.Serializable

@Serializable
object MetroViewModelRecipe : NavigationKey

@Serializable
object MetroModularRecipe : NavigationKey

@Composable
@NavigationDestination(MetroViewModelRecipe::class)
fun MetroViewModelRecipeScreen() {
    val navigation = navigationHandle<MetroViewModelRecipe>()
    RecipeScaffold(
        title = "Metro ViewModel",
        navigation = navigation,
    ) { modifier ->
        MetroViewModelRecipeContent(modifier)
    }
}

@Composable
@NavigationDestination(MetroModularRecipe::class)
fun MetroModularRecipeScreen() {
    val navigation = navigationHandle<MetroModularRecipe>()
    RecipeScaffold(
        title = "Metro Modular",
        navigation = navigation,
    ) { modifier ->
        MetroModularRecipeContent(modifier)
    }
}
