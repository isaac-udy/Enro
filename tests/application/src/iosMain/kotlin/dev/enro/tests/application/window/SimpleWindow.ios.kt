package dev.enro.tests.application.window

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
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
        TitledColumn(
            title = "Simple Window",
            modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            Button(onClick = { navigation.present(SimpleWindow) }) {
                Text(text = "Open Simple Window again")
            }
            Button(onClick = { navigation.requestClose() }) {
                Text(text = "Close Window")
            }
        }
    }
}