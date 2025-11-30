
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.application
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.close
import dev.enro.platform.desktop.GenericRootWindow
import dev.enro.platform.desktop.RootWindow
import dev.enro.platform.desktop.openWindow
import dev.enro.tests.application.SelectDestination
import dev.enro.tests.application.TestApplicationComponent
import dev.enro.tests.application.installNavigationController
import dev.enro.ui.EnroApplicationContent
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer

fun main() {
    val controller = TestApplicationComponent.installNavigationController(Unit)
    controller.openWindow(
        GenericRootWindow(
            windowConfiguration = {
                RootWindow.WindowConfiguration(
                    title = "Enro Test Application",
                    onCloseRequest = { navigation.close() },
                    onKeyEvent = {
                        if (it.type == KeyEventType.KeyDown && it.key == Key.W && it.isMetaPressed) {
                            navigation.close()
                            true
                        }
                        if (it.type == KeyEventType.KeyDown && it.key == Key.Escape) {
                            backDispatcher.onBack()
                        }
                        false
                    }
                )
            }
        ) {
            MenuBar {
                Menu("Window") {
                    Item(
                        "Back",
                        shortcut = KeyShortcut(
                            key = Key.LeftBracket,
                            meta = true
                        )
                    ) {
                        backDispatcher.onBack()
                    }
                    Item(
                        "Close",
                        shortcut = KeyShortcut(
                            key = Key.W,
                            meta = true
                        )
                    ) {
                        navigation.close()
                    }
                }
            }
            val container = rememberNavigationContainer(
                backstack = backstackOf(SelectDestination().asInstance())
            )
            NavigationDisplay(container)
        }
    )
    application {
        EnroApplicationContent(controller)
    }
}
