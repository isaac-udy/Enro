package dev.enro.platform.desktop

import androidx.compose.runtime.Composable
import dev.enro.NavigationKey
import dev.enro.asInstance
import dev.enro.platform.desktop.RootWindow.WindowConfiguration
import kotlinx.serialization.Serializable


@Suppress("FunctionName") // Mimics constructor
public fun GenericRootWindow(
    windowConfiguration: RootWindow<*>.() -> WindowConfiguration = { WindowConfiguration() },
    content: @Composable RootWindowScope<*>.() -> Unit,
): RootWindow<*> {
    return RootWindow(
        instance = GenericRootWindowKey.asInstance(),
        windowConfiguration = windowConfiguration,
        content = content,
    )
}

@Serializable
internal object GenericRootWindowKey : NavigationKey