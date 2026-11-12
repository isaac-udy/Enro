// Desktop snippets for docs/ghpages/docs/getting-started/installation.md
package dev.enro.docs.gettingstarted.installation

import androidx.compose.ui.window.application
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.platform.desktop.GenericRootWindow
import dev.enro.platform.desktop.RootWindow
import dev.enro.platform.desktop.openWindow
import dev.enro.ui.EnroApplicationContent
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer

// region docs:installation/desktop
fun main() {
    val controller = MyComponent.installNavigationController(Unit)
    controller.openWindow(
        GenericRootWindow(
            windowConfiguration = {
                RootWindow.WindowConfiguration(
                    title = "My App",
                    onCloseRequest = { navigation.close() },
                )
            },
        ) {
            val container = rememberNavigationContainer(
                backstack = backstackOf(Home.asInstance()),
            )
            NavigationDisplay(state = container)
        },
    )

    application {
        EnroApplicationContent(controller)
    }
}
// endregion
