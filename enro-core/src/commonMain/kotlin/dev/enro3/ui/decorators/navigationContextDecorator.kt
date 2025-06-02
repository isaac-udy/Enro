package dev.enro3.ui.decorators

import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.enro3.NavigationBackstack
import dev.enro3.NavigationContext
import dev.enro3.NavigationKey
import dev.enro3.handle.NavigationHandleHolder
import dev.enro3.ui.LocalNavigationContainer
import dev.enro3.ui.LocalNavigationContext
import dev.enro3.ui.LocalNavigationHandle

/**
 * Creates a [NavigationDestinationDecorator] that provides navigation context to destinations.
 *
 * This decorator establishes the navigation context for each destination, including:
 * - Lifecycle management based on the destination's position in the backstack
 * - ViewModelStore ownership
 * - Navigation handle binding
 * - Container hierarchy
 *
 * @param backstack The current navigation backstack
 * @param isSettled Whether the navigation state has settled (no animations in progress)
 * @return A decorator that provides navigation context
 */
internal fun <T: NavigationKey> navigationContextDecorator(
    backstack: NavigationBackstack,
    isSettled: Boolean,
): NavigationDestinationDecorator<T> {
    return navigationDestinationDecorator { destination ->
        val container = LocalNavigationContainer.current
        val isInBackstack = backstack.contains(destination.instance)

        // Determine the appropriate lifecycle state based on destination visibility
        val maxLifecycle = when {
            isInBackstack && isSettled -> Lifecycle.State.RESUMED
            isInBackstack && !isSettled -> Lifecycle.State.STARTED
            else /* !isInBackStack */ -> Lifecycle.State.CREATED
        }

        val lifecycleOwner = rememberNavigationLifecycleOwner(
            maxLifecycle = maxLifecycle,
            parentLifecycleOwner = LocalLifecycleOwner.current
        )

        val localViewModelStoreOwner = LocalViewModelStoreOwner.current
        requireNotNull(localViewModelStoreOwner) {
            "No ViewModelStoreOwner available. Ensure ViewModelStoreDecorator is applied."
        }
        require(localViewModelStoreOwner is HasDefaultViewModelProviderFactory) {
            "ViewModelStoreOwner must implement HasDefaultViewModelProviderFactory"
        }

        // Get or create the NavigationHandleHolder for this destination
        val navigationHandleHolder = viewModel<NavigationHandleHolder<T>>(
            viewModelStoreOwner = localViewModelStoreOwner,
        ) {
            NavigationHandleHolder(instance = destination.instance)
        }

        // Create the navigation context for this destination
        val context = NavigationContext(
            lifecycleOwner = lifecycleOwner,
            viewModelStoreOwner = localViewModelStoreOwner,
            defaultViewModelProviderFactory = localViewModelStoreOwner,
            destination = destination,
            parentContainer = container,
            childContainers = emptyList() // TODO: Support child containers
        )

        navigationHandleHolder.bindContext(context)

        // Provide all necessary composition locals
        CompositionLocalProvider(
            LocalNavigationContext provides context,
            LocalLifecycleOwner provides context,
            LocalViewModelStoreOwner provides context,
            LocalNavigationHandle provides navigationHandleHolder.navigationHandle,
        ) {
            destination.Content()
        }
    }
}
