package dev.enro.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.enro.EnroController
import dev.enro.NavigationBackstack
import dev.enro.NavigationKey
import dev.enro.ui.decorators.NavigationSavedStateHolder
import dev.enro.ui.decorators.decorateNavigationDestination
import dev.enro.ui.decorators.rememberLifecycleDecorator
import dev.enro.ui.decorators.rememberMovableContentDecorator
import dev.enro.ui.decorators.rememberNavigationContextDecorator
import dev.enro.ui.decorators.rememberRemovalTrackingDecorator
import dev.enro.ui.decorators.rememberSavedStateDecorator
import dev.enro.ui.decorators.rememberSharedElementDecorator
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
    backstack: NavigationBackstack,
    savedStateHolder: NavigationSavedStateHolder,
    isSettled: Boolean,
): List<NavigationDestination<NavigationKey>> {
    // Create decorators that wrap destinations with additional functionality
    val controllerDecorators = remember {
        controller.decorators.getDecorators()
    }
    val decorators = listOf(
        // sharedElementDecorator must be the OUTERMOST decorator (first
        // in this list — foldRight makes the first entry the outermost
        // wrapper). It always emits a Box(Modifier.sharedElement(...))
        // for every destination in every scene that lists it, even when
        // the movableContentDecorator below skips rendering the actual
        // content. That empty Box is what lets Compose's
        // SharedTransitionScope bridge an entry's bounds from one
        // scene's layout slot to another during a transition.
        rememberSharedElementDecorator(),
        rememberMovableContentDecorator(),  // Preserves content across recompositions
        rememberSavedStateDecorator(savedStateHolder),      // Manages saved instance state
        rememberViewModelStoreDecorator(),  // Provides ViewModelStore for each destination
        rememberLifecycleDecorator(backstack, isSettled),  // Manages lifecycle state
        rememberNavigationContextDecorator(),  // Provides navigation context
    ).plus(controllerDecorators)

    // Add removal tracking decorator last to ensure it tracks all other decorators
    val decoratorsWithRemovalTracking = decorators + rememberRemovalTrackingDecorator(decorators)
    val decoratedDestinations = remember {
        mutableMapOf<String, NavigationDestination<NavigationKey>>()
    }

    return remember(backstack) {
        val active = backstack.map { it.id }
        decoratedDestinations.filter { it.key !in active }
            .onEach { decoratedDestinations.remove(it.key) }

        backstack
            .map { instance ->
                decoratedDestinations.getOrPut(instance.id) {
                    // Find the navigation binding for this instance and create the destination
                    val destination = controller.bindings.destinationFor(instance)
                    decorateNavigationDestination(
                        destination = destination,
                        decorators = decoratorsWithRemovalTracking,
                    )
                }
            }
    }
}

