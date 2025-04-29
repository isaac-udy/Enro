package dev.enro.tests.application.window

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import dev.enro.annotations.NavigationDestination
import dev.enro.core.compose.navigationHandle
import dev.enro.core.requestClose
import dev.enro.destination.desktop.DesktopWindow
import dev.enro.tests.application.compose.common.TitledColumn

@NavigationDestination(SimpleWindow::class)
class SimpleWindowDestination : DesktopWindow() {
    @Composable
    override fun Render() {
        val navigation = navigationHandle()
        Window(
            onCloseRequest = navigation::requestClose,
            title = "Simple Window",
        ) {
            TitledColumn(title = "Simple Window") {
                Button(onClick = { navigation.requestClose() }) {
                    Text(text = "Close Window")
                }
            }
        }
    }
}