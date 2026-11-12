package dev.enro.recipes

import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.platform.EnroUIViewController
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

/** Installs Enro for the recipes. Called once from the Swift AppDelegate. */
@Suppress("unused") // called from Swift
fun installRecipes(application: UIApplication) {
    RecipesComponent.installNavigationController(application)
}

@Suppress("unused") // called from Swift
fun MainViewController(): UIViewController = EnroUIViewController {
    val container = rememberNavigationContainer(
        backstack = backstackOf(SelectRecipe.asInstance()),
    )
    NavigationDisplay(
        state = container,
    )
}
