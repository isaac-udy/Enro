package dev.enro.destination.desktop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.acceptNone
import dev.enro.core.container.backstackOf
import dev.enro.core.requestClose
import kotlinx.serialization.Serializable

@Serializable
internal class OpenComposableInDesktopWindow(
    val instruction: NavigationInstruction.Open<*>
) : NavigationKey.SupportsPresent

internal class DesktopWindowHostForComposable : DesktopWindow() {
    @Composable
    override fun Render() {
        val navigation = navigationHandle<OpenComposableInDesktopWindow>()
        Window(
            onCloseRequest = navigation::requestClose,
            title = "",
        ) {
            val container = rememberNavigationContainer(
                initialBackstack = backstackOf(navigation.key.instruction),
                emptyBehavior = EmptyBehavior.CloseParent,
                filter = acceptNone(),
            )
            Box(modifier = Modifier.fillMaxSize()) {
                container.Render()
            }
        }
    }
}