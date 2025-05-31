package dev.enro3

import androidx.compose.runtime.Composable
import kotlin.jvm.JvmSuppressWildcards


public class NavigationDestinationProvider<T : NavigationKey>(
    private val metadata: Map<String, Any> = emptyMap(),
    private val content: @Composable () -> Unit,
) {
    public fun create(instruction: NavigationKey.Instance<T>): NavigationDestination<T> {
        return object : NavigationDestination<T>(
            instance = instruction,
            metadata = metadata,
            content = content,
        ) {}
    }
}
public open class NavigationDestination<T : NavigationKey>(
    public val instance: NavigationKey.Instance<T>,
    public val metadata: Map<String, Any> = emptyMap(),
    public val content: @Composable () -> Unit,
)

public class NavigationDestinationWrapper<T : NavigationKey>(
    public val destination: NavigationDestination<T>,
    wrapper: @Composable (entry: NavigationDestination<T>) -> Unit,
) : NavigationDestination<T>(
    instance = destination.instance,
    metadata = destination.metadata,
    content = { wrapper(destination) }
)

public open class NavigationDestinationDecorator<T : NavigationKey>(
    internal val onPop: (key: Any) -> Unit,
    internal val decorator: @Composable (entry: NavigationDestination<T>) -> Unit,
)

public fun <T : NavigationKey> navigationDestinationDecorator(
    onPop: (key: Any) -> Unit = {},
    decorator: @Composable (entry: NavigationDestination<T>) -> Unit,
): NavigationDestinationDecorator<T> = NavigationDestinationDecorator(onPop, decorator)

public fun <T : NavigationKey> decorateNavigationDestination(
    destination: NavigationDestination<T>,
    destinationDecorators: List<@JvmSuppressWildcards NavigationDestinationDecorator<*>>,
): NavigationDestination<T> {
    @Suppress("UNCHECKED_CAST")
    return (destinationDecorators as List<@JvmSuppressWildcards NavigationDestinationDecorator<T>>)
        .distinct()
        .foldRight(initial = destination) { decorator, destination ->
            NavigationDestinationWrapper(
                destination = destination,
                wrapper = { wrappedDestination ->
                    decorator.decorator(wrappedDestination)
                }
            )
        }
}

// We probably want to get rid of push/present and let scenes handle those

public fun <T: NavigationKey> navigationDestination(
    metadata: Map<String, Any> = emptyMap(),
    content: @Composable () -> Unit
): NavigationDestinationProvider<T> {
    return NavigationDestinationProvider(metadata, content)
}
