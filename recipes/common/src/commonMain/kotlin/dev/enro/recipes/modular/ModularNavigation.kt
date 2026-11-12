/**
 * Enro Recipe: Modular Navigation
 *
 * Demonstrates navigation between feature modules that do not depend on each
 * other, without a DI framework. The recipe is three Gradle modules:
 *
 *  - `:recipes:modular:navigation` holds the NavigationKeys. It depends on
 *    `enro-common` only.
 *  - `:recipes:modular:feature-home` has the home screen. It opens the
 *    profile, settings and login screens by key.
 *  - `:recipes:modular:feature-profile` has those three screens.
 *
 * The two feature modules depend on the navigation module and not on each
 * other. Each applies the `enro-processor` KSP dependency, which generates a
 * binding for every `@NavigationDestination` in the module.
 *
 * This module (`:recipes:common`) is the app module. It depends on both
 * feature modules, and its `@NavigationComponent` (`RecipesComponent`) picks
 * up their generated bindings from the classpath. Nothing else registers the
 * feature destinations.
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
        // HomeKey comes from the navigation module. Its destination is in
        // feature-home, which this file never references.
        val container = rememberNavigationContainer(
            backstack = backstackOf(HomeKey.asInstance()),
        )
        NavigationDisplay(
            state = container,
            modifier = modifier,
        )
    }
}
