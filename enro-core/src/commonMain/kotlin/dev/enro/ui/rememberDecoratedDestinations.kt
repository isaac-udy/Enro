package dev.enro.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.enro.EnroController
import dev.enro.NavigationKey
import dev.enro.ui.decorators.decorateNavigationDestination
import dev.enro.ui.decorators.rememberLifecycleDecorator
import dev.enro.ui.decorators.rememberMovableContentDecorator
import dev.enro.ui.decorators.rememberNavigationContextDecorator
import dev.enro.ui.decorators.rememberRemovalTrackingDecorator
import dev.enro.ui.decorators.rememberSavedStateDecorator
import dev.enro.ui.decorators.rememberViewModelStoreDecorator


/**
 * Creates NavigationDestination instances from the backstack and applies decorators.
 * Decorators add functionality like lifecycle management, view models, and saved state.
 *
 * @param controller The navigation controller for binding resolution
 * @param backstack The current navigation backstack
 * @param isSettled Whether animations are currently settled (used by lifecycle decorator)
 * @return List of decorated NavigationDestination instances
 */
@Composable
internal fun rememberDecoratedDestinations(
    controller: EnroController,
    backstack: List<NavigationKey.Instance<*>>,
    isSettled: Boolean,
): List<NavigationDestination<NavigationKey>> {
    // Create decorators that wrap destinations with additional functionality
    val decorators = listOf(
        rememberMovableContentDecorator(),  // Preserves content across recompositions
        rememberSavedStateDecorator(),      // Manages saved instance state
        rememberViewModelStoreDecorator(),  // Provides ViewModelStore for each destination
        rememberLifecycleDecorator(backstack, isSettled),  // Manages lifecycle state
        rememberNavigationContextDecorator(),  // Provides navigation context
    )

    // Add removal tracking decorator last to ensure it tracks all other decorators
    val decoratorsWithRemovalTracking = decorators + rememberRemovalTrackingDecorator(decorators)

    return remember(backstack) {
        backstack
            .map { instance ->
                // Find the navigation binding for this instance and create the destination
                @Suppress("UNCHECKED_CAST")
                instance as NavigationKey.Instance<NavigationKey>
                val binding = controller.bindings.bindingFor(instance)
                binding.provider.create(instance)
            }
            .map {
                // Apply all decorators to enhance the destination
                decorateNavigationDestination(
                    destination = it,
                    decorators = decoratorsWithRemovalTracking,
                )
            }
    }
}

