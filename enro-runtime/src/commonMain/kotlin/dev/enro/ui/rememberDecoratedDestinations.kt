package dev.enro.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import dev.enro.EnroController
import dev.enro.NavigationBackstack
import dev.enro.NavigationKey
import dev.enro.ui.decorators.NavigationDestinationDecorator
import dev.enro.ui.decorators.NavigationSavedStateHolder
import dev.enro.ui.decorators.decorateNavigationDestination
import dev.enro.ui.decorators.destinationWrapperDecorator
import dev.enro.ui.decorators.rememberCompositionTrackingDecorator
import dev.enro.ui.decorators.rememberLifecycleDecorator
import dev.enro.ui.decorators.rememberMovableContentDecorator
import dev.enro.ui.decorators.rememberNavigationContextDecorator
import dev.enro.ui.decorators.rememberSavedStateDecorator
import dev.enro.ui.decorators.rememberViewModelStoreDecorator


/**
 * Returns a decorated [NavigationDestination] for each instance in [backstack].
 *
 * A decorator's `onPop` is called once the instance is in neither the backstack nor the
 * composition. `compositionTrackingDecorator` tracks the composition and [PrepareBackStack]
 * tracks the backstack. Navigation 3 uses the same pair of effects.
 */
@Composable
internal fun rememberDecoratedDestinations(
    controller: EnroController,
    backstack: NavigationBackstack,
    savedStateHolder: NavigationSavedStateHolder,
    isSettled: Boolean,
): List<NavigationDestination<NavigationKey>> {
    val controllerDecorators = remember {
        controller.decorators.getDecorators()
    }
    val decorators = listOf(
        // Order matters: the first decorator is the outermost wrapper. The renderer's wrapper
        // must be outside movableContentDecorator, so that the wrapper is composed in every
        // scene that lists the destination, including a scene where movableContentDecorator
        // skips the content. NavigationDisplay's shared element bounds depend on that wrapper.
        remember { destinationWrapperDecorator() },
        rememberMovableContentDecorator(),
        rememberSavedStateDecorator(savedStateHolder),
        rememberViewModelStoreDecorator(),
        rememberLifecycleDecorator(backstack, isSettled),
        rememberNavigationContextDecorator(),
    ).plus(controllerDecorators)

    val idsInBackstack: MutableSet<String> = remember { mutableSetOf() }
    val idsInComposition: MutableSet<String> = remember { mutableSetOf() }

    // The tracking decorator must be last in the list (innermost), so that the tracking
    // decorator's DisposableEffect is inside the movable content. See docs/NAV3-COMPARISON.md.
    val trackingDecorator = rememberCompositionTrackingDecorator(
        decoratorsToInvokeOnPop = decorators,
        idsInBackstack = idsInBackstack,
        idsInComposition = idsInComposition,
    )
    val decoratorsWithTracking = decorators + trackingDecorator
    val decoratedDestinations = remember {
        mutableMapOf<String, NavigationDestination<NavigationKey>>()
    }

    val decorated = remember(backstack) {
        val active = backstack.map { it.id }
        decoratedDestinations.filter { it.key !in active }
            .onEach { decoratedDestinations.remove(it.key) }

        backstack
            .map { instance ->
                decoratedDestinations.getOrPut(instance.id) {
                    val destination = controller.bindings.destinationFor(instance)
                    decorateNavigationDestination(
                        destination = destination,
                        decorators = decoratorsWithTracking,
                    )
                }
            }
    }

    PrepareBackStack(decorated, decorators, idsInBackstack, idsInComposition)
    return decorated
}

/**
 * Runs a [DisposableEffect] for each entry, keyed on the entry id and the entries list. When
 * the effect is disposed for an entry that is in neither the latest entries nor the
 * composition, each decorator's `onPop` is called, last decorator first.
 *
 * `compositionTrackingDecorator` handles the other order, where the entry leaves the
 * composition after the entry leaves the backstack. Navigation 3 equivalent: `PrepareBackStack`.
 */
@Composable
private fun PrepareBackStack(
    entries: List<NavigationDestination<NavigationKey>>,
    decorators: List<NavigationDestinationDecorator<*>>,
    idsInBackstack: MutableSet<String>,
    idsInComposition: MutableSet<String>,
) {
    val latestEntries by rememberUpdatedState(entries)
    val latestDecorators by rememberUpdatedState(decorators)
    entries.forEach { entry ->
        val instance = entry.instance
        val id = instance.id
        idsInBackstack.add(id)
        DisposableEffect(id, entries.toList()) {
            onDispose {
                val latestIds = latestEntries.map { it.instance.id }
                val popped = if (id !in latestIds) idsInBackstack.remove(id) else false
                if (popped && id !in idsInComposition) {
                    @Suppress("UNCHECKED_CAST")
                    (latestDecorators.distinct() as List<NavigationDestinationDecorator<NavigationKey>>)
                        .asReversed()
                        .forEach { it.onPop(instance) }
                }
            }
        }
    }
}

