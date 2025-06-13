package dev.enro.tests.application.window

import androidx.compose.material.Button
import androidx.compose.material.Text
import dev.enro.annotations.NavigationDestination
import dev.enro.close
import dev.enro.tests.application.compose.common.TitledColumn
import dev.enro.ui.destinations.rootWindowDestination

@NavigationDestination(SimpleWindow::class)
val simpleWindowDestination = rootWindowDestination<SimpleWindow> {
    TitledColumn(title ="Simple Window") {
        Button(
            onClick = {
                navigation.close()
            }
        ) {
            Text("Close")
        }
    }
}