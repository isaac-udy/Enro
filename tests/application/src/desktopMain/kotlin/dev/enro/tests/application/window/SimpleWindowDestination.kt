package dev.enro.tests.application.window

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.FrameWindowScope
import dev.enro.annotations.NavigationDestination
import dev.enro.desktop.RootWindow
import dev.enro.desktop.openWindow
import dev.enro.tests.application.compose.common.TitledColumn
import dev.enro.ui.destinations.syntheticDestination

@NavigationDestination(SimpleWindow::class)
val simpleWindowDestination = syntheticDestination<SimpleWindow>() {
    context.controller.openWindow(SimpleWindowImpl())
}

class SimpleWindowImpl : RootWindow() {
    @Composable
    override fun FrameWindowScope.Content() {
        TitledColumn(title ="Simple Window") {
            Button(
                onClick = {
                    close()
                }
            ) {
                Text("Close")
            }
        }
    }
}