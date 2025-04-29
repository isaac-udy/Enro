package dev.enro.tests.application

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.window.Window
import dev.enro.annotations.NavigationDestination
import dev.enro.core.NavigationKey
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.leafContext
import dev.enro.core.requestClose
import dev.enro.destination.compose.navigationContext
import dev.enro.destination.desktop.DesktopWindow
import kotlinx.serialization.Serializable

@Serializable
object ExampleApplicationWindow : NavigationKey.SupportsPresent

@NavigationDestination(ExampleApplicationWindow::class)
class ExampleApplicationWindowDestination : DesktopWindow() {
    @Composable
    override fun Render() {
        val navigationHandle = navigationHandle()
        val context = navigationContext
        Window(
            onKeyEvent = {
                if (it.key == Key.Escape && it.type == KeyEventType.KeyUp) {
                    context.leafContext().navigationHandle.requestClose()
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
                emptyBehavior = EmptyBehavior.CloseParent,
            )

            Box(
                modifier = Modifier
                    .background(MaterialTheme.colors.background)
                    .fillMaxSize()
            ) {
                container.Render()
            }
        }
    }
}
