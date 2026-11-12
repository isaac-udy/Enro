/**
 * A [NavigationDestinationDecorator] wraps the content of each navigation destination.
 * `rememberNavigationContainer` applies the decorators below. A custom renderer that does not
 * call `rememberNavigationContainer` applies the decorators with [decorateNavigationDestination]:
 *
 * ```kotlin
 * val decoratedDestination = decorateNavigationDestination(
 *     destination = originalDestination,
 *     decorators = listOf(
 *         rememberMovableContentDecorator(),
 *         rememberSavedStateDecorator(navigationSavedStateHolder),
 *         rememberViewModelStoreDecorator(),
 *         rememberLifecycleDecorator(backstack, isSettled),
 *         rememberNavigationContextDecorator(),
 *     )
 * )
 * ```
 *
 * The first decorator in the list is the outermost wrapper. The order above is required:
 * 1. [movableContentDecorator] is before every decorator that remembers state for the
 *    destination.
 * 2. [savedStateDecorator] is before [viewModelStoreDecorator], which requires the
 *    `SavedStateRegistryOwner` for `SavedStateHandle` support.
 * 3. [viewModelStoreDecorator] and [navigationLifecycleDecorator] are before
 *    [navigationContextDecorator], which reads the `ViewModelStoreOwner` and the
 *    `LifecycleOwner` that the two decorators provide.
 *
 * [decorateNavigationDestination] does not call a decorator's `onPop`.
 *
 * Create a decorator with [navigationDestinationDecorator]:
 *
 * ```kotlin
 * fun myCustomDecorator(): NavigationDestinationDecorator<NavigationKey> {
 *     return navigationDestinationDecorator(
 *         onPop = { instance ->
 *             // Called once the instance is in neither the backstack nor the composition.
 *         },
 *         decorate = { destination ->
 *             MyCustomWrapper {
 *                 destination.Content()
 *             }
 *         }
 *     )
 * }
 * ```
 */
package dev.enro.ui.decorators
