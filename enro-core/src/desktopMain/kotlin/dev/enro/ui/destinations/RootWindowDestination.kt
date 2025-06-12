package dev.enro.ui.destinations

import androidx.compose.runtime.Composable
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.context.RootContext
import dev.enro.desktop.RootWindow
import dev.enro.desktop.RootWindowScope
import dev.enro.desktop.createRootWindow
import dev.enro.desktop.openWindow
import dev.enro.navigationHandle
import dev.enro.ui.NavigationDestinationProvider
import dev.enro.ui.navigationDestination
import kotlin.reflect.KClass


public object RootWindowDestination {
    internal const val WindowConfigurationKey = "dev.enro.ui.destinations.RootWindowDestination.WindowConfigurationKey"
    internal const val ContentKey = "dev.enro.ui.destinations.RootWindowDestination.ContentKey"

    internal fun openAsRootWindow(
        context: RootContext,
        instance: NavigationKey.Instance<NavigationKey>,
    ) {
        val metadata = context.controller.bindings.bindingFor(instance)
            .provider
            .peekMetadata(instance)

        val windowConfiguration = metadata[WindowConfigurationKey] as RootWindow.() -> RootWindow.WindowConfiguration?

        @Suppress("UNCHECKED_CAST")
        val contentContainer = metadata[ContentKey] as? ContentContainer<NavigationKey>
        if (contentContainer== null) {
            error("RootWindowDestination requires a content block.")
        }
        val content = contentContainer.content
        context.controller.openWindow(
            createRootWindow(
                instance = instance,
                windowConfiguration = windowConfiguration,
            ) {
                RootWindowDestinationScope(
                    navigation = navigationHandle<NavigationKey>(),
                    rootWindowScope = this,
                ).content()
            }
        )
    }

    internal class ContentContainer<T : NavigationKey>(
        val content: @Composable RootWindowDestinationScope<T>.() -> Unit,
    )
}

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
    return navigationDestination(
        metadata = {
            add(RootWindowDestination.WindowConfigurationKey to windowConfiguration)
            add(
                RootWindowDestination.ContentKey to RootWindowDestination.ContentContainer(content)
            )
            rootContextDestination()
        }
    ) {
        error("activityDestination should not be rendered directly. If you are reaching this, please report this as a bug.")
    }
}

public class RootWindowDestinationScope<T : NavigationKey>(
    public val navigation: NavigationHandle<T>,
    private val rootWindowScope: RootWindowScope,
) : RootWindowScope(rootWindowScope) {
    public val instance: NavigationKey.Instance<T> get() = navigation.instance
    public val key: T get() = instance.key
    public val id: String get() = instance.id
}
