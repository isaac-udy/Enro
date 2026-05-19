package dev.enro.ui.decorators

import androidx.compose.runtime.Composable
import dev.enro.NavigationKey
import dev.enro.ui.NavigationDestination

/**
 * A decorator that wraps navigation destinations to provide additional functionality
 * such as lifecycle management, state preservation, or visual effects.
 *
 * Parameter names ([onPop], [decorate]) mirror Nav3's `NavEntryDecorator` so a
 * decorator written against Nav3 can almost copy-paste over. See
 * `docs/NAV3-COMPARISON.md` for the broader alignment rationale.
 *
 * @param T The type of NavigationKey this decorator can handle
 * @property onPop Called when the destination is popped from the backstack and
 *   has left composition. Mirrors Nav3's `NavEntryDecorator.onPop`.
 * @property decorate The composable function that wraps the destination content.
 *   Mirrors Nav3's `NavEntryDecorator.decorate`.
 */
public open class NavigationDestinationDecorator<T : NavigationKey>(
    internal val onPop: (key: NavigationKey.Instance<T>) -> Unit,
    internal val decorate: @Composable (destination: NavigationDestination<T>) -> Unit,
)

/**
 * Creates a [NavigationDestinationDecorator] with the provided lifecycle callback and decorate function.
 *
 * @param onPop Called when the destination is popped from the backstack and has left composition.
 * @param decorate The composable function that wraps the destination content.
 */
public fun <T : NavigationKey> navigationDestinationDecorator(
    onPop: (key: NavigationKey.Instance<T>) -> Unit = {},
    decorate: @Composable (destination: NavigationDestination<T>) -> Unit,
): NavigationDestinationDecorator<T> = NavigationDestinationDecorator(onPop, decorate)

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
                content = { decorator.decorate(dest) }
            )
        }
}