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
import dev.enro.ui.decorators.rememberLifecycleDecorator
import dev.enro.ui.decorators.rememberMovableContentDecorator
import dev.enro.ui.decorators.rememberNavigationContextDecorator
import dev.enro.ui.decorators.rememberSavedStateDecorator
import dev.enro.ui.decorators.rememberSharedElementDecorator
import dev.enro.ui.decorators.rememberViewModelStoreDecorator


/**
 * Creates NavigationDestination instances from the backstack and applies decorators.
 * Decorators add functionality like lifecycle management, view models, and saved state.
 *
 * Onpop tracking mirrors Nav3's PrepareBackStack + per-entry DisposableEffect pattern:
 * each decorated destination's content() runs a DisposableEffect that tracks composition
 * presence, and [PrepareBackStack] runs DisposableEffects keyed on the backstack list so
 * leaving the backstack is observable too. `onPop` fires when an entry has left BOTH.
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

    val idsInBackstack: MutableSet<String> = remember { mutableSetOf() }
    val idsInComposition: MutableSet<String> = remember { mutableSetOf() }
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
                        decorators = decorators,
                        idsInBackstack = idsInBackstack,
                        idsInComposition = idsInComposition,
                    )
                }
            }
    }

    PrepareBackStack(decorated, decorators, idsInBackstack, idsInComposition)
    return decorated
}

/**
 * Tracks backstack membership for each decorated destination. Mirrors Nav3's
 * `PrepareBackStack`: every entry gets a [DisposableEffect] keyed on the entry id and
 * the latest backstack list. When the effect disposes (entry left the backstack), if
 * the entry is also not in composition, decorator `onPop` callbacks fire in
 * reverse-decoration order.
 *
 * Splits the lifecycle observability into two independent sources (backstack + composition)
 * so we never miss the moment both have settled, regardless of which goes first.
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

