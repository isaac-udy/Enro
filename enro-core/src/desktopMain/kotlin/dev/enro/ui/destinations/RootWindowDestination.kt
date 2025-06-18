package dev.enro.ui.destinations

import androidx.compose.runtime.Composable
import dev.enro.NavigationKey
import dev.enro.context.RootContext
import dev.enro.platform.desktop.RootWindow
import dev.enro.platform.desktop.RootWindowScope
import dev.enro.platform.desktop.openWindow
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.navigationDestination
import kotlin.reflect.KClass


public object RootWindowDestination {
    internal const val ConfigurationKey = "dev.enro.ui.destinations.RootWindowDestination.ConfigurationKey"

    internal fun openAsRootWindow(
        context: RootContext,
        instance: NavigationKey.Instance<NavigationKey>,
    ) {
        val metadata = context.controller.bindings.bindingFor(instance)
            .provider
            .peekMetadata(instance)

        @Suppress("UNCHECKED_CAST")
        val configuration = metadata[ConfigurationKey] as? RootWindowDestinationConfiguration<NavigationKey>
        if (configuration == null) {
            error("RootWindowDestination requires a content block.")
        }
        context.controller.openWindow(
            RootWindow(
                instance = instance,
                windowConfiguration = configuration.windowConfiguration,
                content = configuration.content,
            )
        )
    }

    internal class RootWindowDestinationConfiguration<T : NavigationKey>(
        val windowConfiguration: RootWindow<T>.() -> RootWindow.WindowConfiguration,
        val content: @Composable RootWindowScope<T>.() -> Unit,
    )
}

public inline fun <reified T : NavigationKey> rootWindowDestination(
    noinline windowConfiguration: RootWindow<T>.() -> RootWindow.WindowConfiguration = { RootWindow.WindowConfiguration() },
    noinline content: @Composable RootWindowScope<T>.() -> Unit,
): NavigationDestinationProvider<T> {
    return rootWindowDestination(T::class, windowConfiguration, content)
}

public fun <T : NavigationKey> rootWindowDestination(
    keyType: KClass<T>,
    windowConfiguration: RootWindow<T>.() -> RootWindow.WindowConfiguration = { RootWindow.WindowConfiguration() },
    content: @Composable RootWindowScope<T>.() -> Unit,
): NavigationDestinationProvider<T> {
    return navigationDestination(
        metadata = {
            add(
                RootWindowDestination.ConfigurationKey to RootWindowDestination.RootWindowDestinationConfiguration(
                    windowConfiguration = windowConfiguration,
                    content = content,
                )
            )
            rootContextDestination()
        }
    ) {
        error("activityDestination should not be rendered directly. If you are reaching this, please report this as a bug.")
    }
}
