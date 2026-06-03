package dev.enro.recipes

import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.platform.EnroUIViewController
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer
import platform.UIKit.UIViewController

@Suppress("unused") // called from Swift
fun MainViewController(): UIViewController = EnroUIViewController {
    val container = rememberNavigationContainer(
        backstack = backstackOf(SelectRecipe.asInstance()),
    )
    NavigationDisplay(
        state = container,
    )
}
