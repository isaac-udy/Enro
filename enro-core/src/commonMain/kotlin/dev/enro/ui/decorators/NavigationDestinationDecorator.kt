package dev.enro.ui.decorators

import androidx.compose.runtime.Composable
import dev.enro.NavigationKey
import dev.enro.ui.NavigationDestination

/**
 * A decorator that wraps navigation destinations to provide additional functionality
 * such as lifecycle management, state preservation, or visual effects.
 *
 * @param T The type of NavigationKey this decorator can handle
 * @property onRemove Called when the destination is removed from the backstack
 * @property decorator The composable function that wraps the destination content
 */
public class NavigationDestinationDecorator<T : NavigationKey>(
    internal val onRemove: (key: NavigationKey.Instance<T>) -> Unit,
    internal val decorator: @Composable (destination: NavigationDestination<T>) -> Unit,
)

/**
 * Creates a [NavigationDestinationDecorator] with the provided lifecycle callback and decorator function.
 *
 * @param onRemove Called when the destination is removed from the backstack
 * @param decorator The composable function that wraps the destination content
 */
public fun <T : NavigationKey> navigationDestinationDecorator(
    onRemove: (key: NavigationKey.Instance<T>) -> Unit = {},
    decorator: @Composable (destination: NavigationDestination<T>) -> Unit,
): NavigationDestinationDecorator<T> = NavigationDestinationDecorator(onRemove, decorator)

/**
 * Applies a list of decorators to a navigation destination, wrapping it in the order provided.
 * Decorators are applied from first to last, meaning the first decorator in the list will be
 * the outermost wrapper.
 *
 * @param destination The destination to decorate
 * @param decorators The list of decorators to apply
 * @return The decorated navigation destination
 */
public fun <T : NavigationKey> decorateNavigationDestination(
    destination: NavigationDestination<T>,
    decorators: List<NavigationDestinationDecorator<*>>,
): NavigationDestination<T> {
    @Suppress("UNCHECKED_CAST")
    return (decorators as List<NavigationDestinationDecorator<T>>)
        .distinct()
        .foldRight(initial = destination) { decorator, dest ->
            NavigationDestination.createWithoutScope(
                instance = destination.instance,
                metadata = destination.metadata,
                content = { decorator.decorator(dest) }
            )
        }
}