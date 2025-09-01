package dev.enro.tests.application.window

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import dev.enro.annotations.NavigationDestination
import dev.enro.core.compose.navigationHandle
import dev.enro.core.requestClose
import dev.enro.destination.web.WebWindow
import dev.enro.tests.application.compose.common.TitledColumn

@NavigationDestination(SimpleWindow::class)
class SimpleWindowDestination : WebWindow() {
    @Composable
    override fun Render() {
        val navigation = navigationHandle()
        TitledColumn(title = "Simple Window") {
            Button(onClick = { navigation.requestClose() }) {
                Text(text = "Close Window")
            }
        }
    }
}