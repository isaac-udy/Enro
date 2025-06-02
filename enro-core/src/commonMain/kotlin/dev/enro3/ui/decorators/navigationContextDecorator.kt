package dev.enro3.ui.decorators

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro3.NavigationContext
import dev.enro3.NavigationKey
import dev.enro3.handle.NavigationHandleHolder
import dev.enro3.ui.LocalNavigationContainer
import dev.enro3.ui.LocalNavigationContext
import dev.enro3.ui.LocalNavigationHandle

/**
 * Returns a [NavigationDestinationDecorator] that provides navigation context to destinations.
 *
 * This decorator establishes the navigation context for each destination, including:
 * - Navigation handle binding
 * - Container hierarchy
 * - Access to lifecycle and ViewModelStore owners from parent decorators
 *
 * **Note:** This decorator requires the following decorators to be applied before it:
 * - [navigationLifecycleDecorator] or [rememberLifecycleDecorator] for lifecycle management
 * - [viewModelStoreDecorator] or [rememberViewModelStoreDecorator] for ViewModel support
 */
@Composable
public fun rememberNavigationContextDecorator(): NavigationDestinationDecorator<NavigationKey> = remember {
    navigationContextDecorator()
}

/**
 * Creates a [NavigationDestinationDecorator] that provides navigation context.
 *
 * This decorator creates and binds the [NavigationContext] for each destination,
 * providing access to navigation functionality through composition locals.
 */
internal fun navigationContextDecorator(): NavigationDestinationDecorator<NavigationKey> {
    return navigationDestinationDecorator { destination ->
        val container = LocalNavigationContainer.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val viewModelStoreOwner = LocalViewModelStoreOwner.current

        requireNotNull(viewModelStoreOwner) {
            "No ViewModelStoreOwner available. Ensure ViewModelStoreDecorator is applied before NavigationContextDecorator."
        }
        require(viewModelStoreOwner is HasDefaultViewModelProviderFactory) {
            "ViewModelStoreOwner must implement HasDefaultViewModelProviderFactory"
        }

        // Get or create the NavigationHandleHolder for this destination
        @Suppress("UNCHECKED_CAST")
        val navigationHandleHolder = viewModel<NavigationHandleHolder<NavigationKey>>(
            viewModelStoreOwner = viewModelStoreOwner,
        ) {
            NavigationHandleHolder(instance = destination.instance as NavigationKey.Instance<NavigationKey>)
        }

        // Create the navigation context for this destination
        val context = NavigationContext(
            lifecycleOwner = lifecycleOwner,
            viewModelStoreOwner = viewModelStoreOwner,
            defaultViewModelProviderFactory = viewModelStoreOwner,
            destination = destination,
            parentContainer = container,
            childContainers = emptyList() // TODO: Support child containers
        )

        navigationHandleHolder.bindContext(context)

        // Provide navigation-specific composition locals
        CompositionLocalProvider(
            LocalNavigationContext provides context,
            LocalNavigationHandle provides navigationHandleHolder.navigationHandle,
        ) {
            destination.Content()
        }
    }
}
