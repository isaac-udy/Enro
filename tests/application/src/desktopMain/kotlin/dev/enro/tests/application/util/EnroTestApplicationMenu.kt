package dev.enro.tests.application.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.MenuBar
import dev.enro.close
import dev.enro.desktop.RootWindowScope

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
}