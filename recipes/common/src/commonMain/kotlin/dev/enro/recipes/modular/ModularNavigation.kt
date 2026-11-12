/**
 * Enro Recipe: Modular Navigation
 *
 * Shows navigation between two feature modules that do not depend on each
 * other, without a DI framework. The recipe has three Gradle modules:
 *
 *  - `:recipes:modular:navigation` declares the NavigationKeys and depends
 *    on `enro-common` only.
 *  - `:recipes:modular:feature-home` declares the home screen, which opens
 *    the profile, settings and login screens by NavigationKey.
 *  - `:recipes:modular:feature-profile` declares the profile, settings and
 *    login screens.
 *
 * Each feature module depends on the navigation module and not on the other
 * feature module. Each feature module applies the `enro-processor` KSP
 * dependency, which generates a binding for each `@NavigationDestination` in
 * the module.
 *
 * `:recipes:common` is the app module and depends on both feature modules.
 * The `@NavigationComponent` of the app module (`RecipesComponent`) loads the
 * generated bindings from the classpath. No other code registers the feature
 * destinations.
 *
 * Nav3 equivalents: the Hilt, Koin and Metro modular recipes.
 */
package dev.enro.recipes.modular

import androidx.compose.runtime.Composable
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.navigationHandle
import dev.enro.recipes.RecipeScaffold
import dev.enro.recipes.modular.navigation.HomeKey
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

@Serializable
object ModularNavigationRecipe : NavigationKey

@Composable
@NavigationDestination(ModularNavigationRecipe::class)
fun ModularNavigationRecipeScreen() {
    val navigation = navigationHandle<ModularNavigationRecipe>()
    RecipeScaffold(
        title = "Modular Navigation",
        navigation = navigation,
    ) { modifier ->
        // The navigation module declares HomeKey. feature-home declares the
        // destination for HomeKey, and this file does not import feature-home.
        val container = rememberNavigationContainer(
            backstack = backstackOf(HomeKey.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
        )
    }
}
