/**
 * Navigation destination decorators provide a composable and extensible way to add functionality
 * to navigation destinations without modifying their implementation.
 *
 * ## Overview
 *
 * Decorators wrap navigation destinations to provide additional functionality such as:
 * - State preservation ([savedStateDecorator])
 * - ViewModel scoping ([viewModelStoreDecorator])
 * - Lifecycle management ([navigationContextDecorator])
 * - Content optimization ([movableContentDecorator])
 *
 * ## Usage
 *
 * Decorators are typically applied automatically by [NavigationDisplay], but can also be
 * applied manually when creating custom navigation displays:
 *
 * ```kotlin
 * val decoratedDestination = decorateNavigationDestination(
 *     destination = originalDestination,
 *     decorators = listOf(
 *         rememberMovableContentDecorator(),
 *         rememberSavedStateDecorator(),
 *         rememberViewModelStoreDecorator(),
 *         navigationContextDecorator(backstack, isSettled)
 *     )
 * )
 * ```
 *
 * ## Order of Application
 *
 * The order in which decorators are applied is important:
 * 1. **movableContentDecorator** - Should be first to ensure other decorators are moved properly
 * 2. **savedStateDecorator** - Required by ViewModelStore decorator for SavedStateHandle support
 * 3. **viewModelStoreDecorator** - Provides ViewModel scoping
 * 4. **navigationContextDecorator** - Should be last as it depends on the others
 *
 * ## Creating Custom Decorators
 *
 * To create a custom decorator, use the [navigationDestinationDecorator] function:
 *
 * ```kotlin
 * fun myCustomDecorator(): NavigationDestinationDecorator<NavigationKey> {
 *     return navigationDestinationDecorator(
 *         onRemove = { instance ->
 *             // Clean up when destination is removed
 *         },
 *         decorator = { destination ->
 *             // Wrap the destination content
 *             MyCustomWrapper {
 *                 destination.Content()
 *             }
 *         }
 *     )
 * }
 * ```
 */
package dev.enro3.ui.decorators

import dev.enro3.ui.NavigationDisplay