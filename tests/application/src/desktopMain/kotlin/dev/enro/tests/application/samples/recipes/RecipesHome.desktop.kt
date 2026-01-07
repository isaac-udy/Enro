package dev.enro.tests.application.samples.recipes

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.MenuBar
import dev.enro.annotations.NavigationDestination
import dev.enro.close
import dev.enro.context.activeLeaf
import dev.enro.context.getNavigationHandle
import dev.enro.open
import dev.enro.requestClose
import dev.enro.ui.destinations.rootWindowDestination

@NavigationDestination.PlatformOverride(RecipesSampleDestination::class)
val recipesHome = rootWindowDestination<RecipesSampleDestination> {
    MenuBar {
        Menu("Recipes", 'R', true) {
            Item("Add Recipe", onClick = {
                navigation.open(CreateRecipe)
            })
            Separator()
            Item("Back", shortcut = KeyShortcut(Key.LeftBracket, meta = true), onClick = {
                navigationContext.activeLeaf().getNavigationHandle().requestClose()
            })
            Item("Exit", shortcut = KeyShortcut(Key.W, meta = true), onClick = { navigation.close() })
        }
    }
    RecipesHomeScreen()
}