package dev.enro.tests.application.samples.recipes

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.MenuBar
import dev.enro.annotations.NavigationDestination
import dev.enro.ui.destinations.rootWindowDestination

@NavigationDestination.PlatformOverride(RecipesHome::class)
val recipesHome = rootWindowDestination<RecipesHome> {
    MenuBar {
        Menu("Recipes", 'R', true) {
            Item("Add Recipe", onClick = { /* Handle add recipe */ })
            Separator()
            Item("Back", shortcut = KeyShortcut(Key.LeftBracket, meta = true), onClick = { backDispatcher.onBack() })
            Item("Exit", shortcut = KeyShortcut(Key.W, meta = true), onClick = { close() })
        }
    }
    RecipesHomeScreen()
}