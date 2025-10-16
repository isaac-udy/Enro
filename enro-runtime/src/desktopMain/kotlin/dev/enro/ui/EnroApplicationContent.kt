package dev.enro.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.ApplicationScope
import dev.enro.EnroController
import dev.enro.NavigationKey
import dev.enro.platform.desktop.RootWindow

@Composable
public fun ApplicationScope.EnroApplicationContent(
    controller: EnroController,
) {
    val contexts = controller.rootContextRegistry.getAllContexts()
    contexts.forEach { context ->
        val parent = context.parent
        if (parent is RootWindow<NavigationKey>) {
            parent.movableWindowContent()
        }
    }
}