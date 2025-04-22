package dev.enro.tests.application

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.requestClose
import dev.enro.destination.desktop.DesktopWindow
import kotlinx.serialization.Serializable

@Serializable
object ExampleApplicationWindow : NavigationKey.SupportsPresent

@NavigationDestination(ExampleApplicationWindow::class)
class ExampleApplicationWindowDestination : DesktopWindow() {
    @Composable
    override fun ApplicationScope.Render() {
        val navigationHandle = navigationHandle()
        Window(
            onKeyEvent = {
                if (it.key == Key.Escape && it.type == KeyEventType.KeyUp) {
                    true
                } else {
                    false
                }
            },
            onCloseRequest = navigationHandle::requestClose,
            title = "Enro Test Application",
        ) {
            val container = rememberNavigationContainer(
                root = SelectDestination,
                emptyBehavior = EmptyBehavior.Action {
                    exitApplication()
                    true
                },
            )

            Box(
                modifier = Modifier
                    .background(Color.LightGray)
                    .fillMaxSize()
            ) {
                container.Render()
            }
        }
    }
}
