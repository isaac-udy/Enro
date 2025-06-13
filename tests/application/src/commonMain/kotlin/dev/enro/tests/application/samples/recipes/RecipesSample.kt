package dev.enro.tests.application.samples.recipes

import androidx.compose.runtime.Composable
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

@Serializable
object RecipesSample : NavigationKey

@Composable
@NavigationDestination(RecipesSample::class)
fun RecipesHomeScreen() {
    val container = rememberNavigationContainer(
        backstack = listOf(RecipeList.asInstance()),
    )
    NavigationDisplay(state = container)
}
