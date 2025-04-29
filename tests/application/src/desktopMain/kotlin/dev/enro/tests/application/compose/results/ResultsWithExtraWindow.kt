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
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.requestClose
import dev.enro.core.useOriginalBinding
import dev.enro.destination.desktop.DesktopWindow

@NavigationDestination(ResultsWithExtra::class)
@PlatformDestination
class ResultsWithExtraWindow : DesktopWindow() {

    @Composable
    override fun Render() {
        val navigation = navigationHandle()
        Window(
            onCloseRequest = navigation::requestClose,
            title = "Simple Window",
        ) {
            val container = rememberNavigationContainer(
                root = ResultsWithExtra.useOriginalBinding(),
                emptyBehavior = EmptyBehavior.CloseParent,
            )
            Box(modifier = Modifier.fillMaxSize()) {
                container.Render()
            }
        }
    }
}
