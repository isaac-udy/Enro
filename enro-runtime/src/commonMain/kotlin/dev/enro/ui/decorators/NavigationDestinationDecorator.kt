package dev.enro.ui.decorators

import androidx.compose.runtime.Composable
import dev.enro.NavigationKey
import dev.enro.ui.NavigationDestination

/**
 * Wraps the content of each navigation destination. Navigation 3 equivalent:
 * `NavEntryDecorator`.
 *
 * @property onPop Called once the destination is in neither the backstack nor the composition.
 * @property decorate Renders the destination. Call [NavigationDestination.Content] on the
 *   destination passed to [decorate] to render the content that the decorator wraps.
 */
public open class NavigationDestinationDecorator<T : NavigationKey>(
    internal val onPop: (key: NavigationKey.Instance<T>) -> Unit,
    internal val decorate: @Composable (destination: NavigationDestination<T>) -> Unit,
)

/**
 * @param onPop Called once the destination is in neither the backstack nor the composition.
 * @param decorate Renders the destination. Call [NavigationDestination.Content] on the
 *   destination passed to [decorate] to render the content that the decorator wraps.
 */
public fun <T : NavigationKey> navigationDestinationDecorator(
    onPop: (key: NavigationKey.Instance<T>) -> Unit = {},
    decorate: @Composable (destination: NavigationDestination<T>) -> Unit,
): NavigationDestinationDecorator<T> = NavigationDestinationDecorator(onPop, decorate)

/**
 * Returns [destination] wrapped in [decorators]. The first decorator is the outermost wrapper.
 * A decorator that is in [decorators] more than once is applied once.
 *
 * [decorateNavigationDestination] does not call a decorator's `onPop`. Inside Enro,
 * `compositionTrackingDecorator` calls `onPop` and must be the last decorator in the list.
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