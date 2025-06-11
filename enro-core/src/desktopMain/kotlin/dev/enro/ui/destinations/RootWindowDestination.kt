package dev.enro.ui.destinations

import androidx.compose.runtime.Composable
import dev.enro.NavigationKey
import dev.enro.desktop.RootWindow
import dev.enro.desktop.RootWindowScope
import dev.enro.desktop.createRootWindow
import dev.enro.desktop.openWindow
import dev.enro.ui.NavigationDestinationProvider
import kotlin.reflect.KClass

public inline fun <reified T : NavigationKey> rootWindowDestination(
    noinline windowConfiguration: RootWindow.() -> RootWindow.WindowConfiguration? = { null },
    noinline content: @Composable RootWindowDestinationScope<T>.() -> Unit,
): NavigationDestinationProvider<T> {
    return rootWindowDestination(T::class, windowConfiguration, content)
}


public fun <T : NavigationKey> rootWindowDestination(
    keyType: KClass<T>,
    windowConfiguration: RootWindow.() -> RootWindow.WindowConfiguration? = { null },
    content: @Composable RootWindowDestinationScope<T>.() -> Unit,
): NavigationDestinationProvider<T> {
    return syntheticDestination {
        context.controller.openWindow(
            createRootWindow(
                windowConfiguration = windowConfiguration,
            ) {
                RootWindowDestinationScope(
                    instance = instance,
                    rootWindowScope = this,
                ).content()
            }
        )
    }
}

public class RootWindowDestinationScope<T : NavigationKey>(
    public val instance: NavigationKey.Instance<T>,
    private val rootWindowScope: RootWindowScope,
) : RootWindowScope(rootWindowScope) {
    public val key: T get() = instance.key
    public val id: String get() = instance.id
}
