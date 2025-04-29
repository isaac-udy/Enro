package dev.enro.tests.application.window

import androidx.compose.material.Button
import androidx.compose.material.Text
import dev.enro.annotations.NavigationDestination
import dev.enro.core.compose.navigationHandle
import dev.enro.core.present
import dev.enro.core.requestClose
import dev.enro.destination.uiviewcontroller.EnroComposeUIViewController
import dev.enro.tests.application.compose.common.TitledColumn
import platform.UIKit.UIViewController

@NavigationDestination(SimpleWindow::class)
fun SimpleWindowViewController(): UIViewController {
    return EnroComposeUIViewController {
        val navigation = navigationHandle()
        TitledColumn(title = "Simple Window") {
            Button(onClick = { navigation.present(SimpleWindow) }) {
                Text(text = "Open Simple Window again")
            }
            Button(onClick = { navigation.requestClose() }) {
                Text(text = "Close Window")
            }
        }
    }
}