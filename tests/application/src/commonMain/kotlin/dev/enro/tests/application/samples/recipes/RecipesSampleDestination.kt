package dev.enro.tests.application.samples.recipes

import androidx.compose.runtime.Composable
import dev.enro.NavigationKey
import dev.enro.annotations.NavigationDestination
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import kotlinx.serialization.Serializable

@Serializable
object RecipesSampleDestination : NavigationKey

@Composable
@NavigationDestination(RecipesSampleDestination::class)
fun RecipesHomeScreen() {
    val container = rememberNavigationContainer(
        backstack = backstackOf(RecipeList.asInstance()),
    )
    NavigationDisplay(state = container)
}
