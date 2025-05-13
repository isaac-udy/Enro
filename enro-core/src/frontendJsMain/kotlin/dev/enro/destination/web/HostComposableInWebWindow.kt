package dev.enro.destination.web

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.compose.navigationHandle
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.acceptNone
import dev.enro.core.container.backstackOf
import kotlinx.serialization.Serializable

@Serializable
internal class HostComposableInWebWindow(
    val instruction: AnyOpenInstruction,
): NavigationKey.SupportsPresent

public class WindowForHostingComposable : WebWindow() {
    @Composable
    override fun Render() {
        val navigation = navigationHandle<HostComposableInWebWindow>()
        val container = rememberNavigationContainer(
            initialBackstack = backstackOf(navigation.key.instruction),
            emptyBehavior = EmptyBehavior.Action {
                kotlinx.browser.window.close()
                false
            },
            filter = acceptNone(),
        )
        Box(modifier = Modifier.fillMaxSize()) {
            container.Render()
        }
    }
}