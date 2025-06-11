package dev.enro.tests.application.samples.recipes

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.MenuBar
import dev.enro.annotations.NavigationDestination
import dev.enro.desktop.createRootWindow
import dev.enro.desktop.openWindow
import dev.enro.ui.destinations.syntheticDestination

@NavigationDestination.PlatformOverride(RecipesHome::class)
val recipesHome = syntheticDestination<RecipesHome> {
    context.controller.openWindow(
        createRootWindow {
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
    )
}