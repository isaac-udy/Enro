package dev.enro.tests.application.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.MenuBar
import dev.enro.close
import dev.enro.context.activeLeaf
import dev.enro.context.getNavigationHandle
import dev.enro.platform.desktop.RootWindowScope
import dev.enro.requestClose

@Composable
fun RootWindowScope<*>.EnroTestApplicationMenu() {
    MenuBar {
        Menu("Window") {
            Item(
                "Back",
                shortcut = KeyShortcut(
                    key = Key.LeftBracket,
                    meta = true
                )
            ) {
                navigationContext.activeLeaf().getNavigationHandle().requestClose()
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
}