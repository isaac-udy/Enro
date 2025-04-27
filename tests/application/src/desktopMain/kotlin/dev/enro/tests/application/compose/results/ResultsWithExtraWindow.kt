package dev.enro.tests.application.compose.results

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import dev.enro.annotations.NavigationDestination
import dev.enro.annotations.PlatformDestination
import dev.enro.core.compose.navigationHandle
import dev.enro.core.requestClose
import dev.enro.destination.desktop.DesktopWindow

@NavigationDestination(ResultsWithExtra::class)
@PlatformDestination
class ResultsWithExtraWindow : DesktopWindow() {

    @Composable
    override fun ApplicationScope.Render() {
        val navigation = navigationHandle()
        Window(
            onCloseRequest = navigation::requestClose,
            title = "Simple Window",
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                ResultsWithExtraScreen()
            }
        }
    }
}
