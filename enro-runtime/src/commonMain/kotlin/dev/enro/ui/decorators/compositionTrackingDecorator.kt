package dev.enro.ui.decorators

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import dev.enro.NavigationKey

/**
 * Records each destination that is in the composition in [idsInComposition]. When a
 * destination leaves the composition and is not in [idsInBackstack], calls `onPop` on each of
 * [decoratorsToInvokeOnPop], last decorator first. `PrepareBackStack` in
 * `rememberDecoratedDestinations` handles a destination that leaves the backstack after
 * leaving the composition.
 *
 * Must be the last decorator in the list (innermost). The decorator's [DisposableEffect] is
 * then inside the movable content from [movableContentDecorator], and is disposed in the same
 * pass as the providers of the other decorators. An effect outside the movable content is
 * disposed earlier, and a recomposition could then read state that `onPop` has already
 * cleared. See `docs/NAV3-COMPARISON.md`.
 */
@Composable
internal fun rememberCompositionTrackingDecorator(
    decoratorsToInvokeOnPop: List<NavigationDestinationDecorator<*>>,
    idsInBackstack: MutableSet<String>,
    idsInComposition: MutableSet<String>,
): NavigationDestinationDecorator<NavigationKey> =
    remember(decoratorsToInvokeOnPop, idsInBackstack, idsInComposition) {
        compositionTrackingDecorator(decoratorsToInvokeOnPop, idsInBackstack, idsInComposition)
    }

internal fun compositionTrackingDecorator(
    decoratorsToInvokeOnPop: List<NavigationDestinationDecorator<*>>,
    idsInBackstack: MutableSet<String>,
    idsInComposition: MutableSet<String>,
): NavigationDestinationDecorator<NavigationKey> {
    return navigationDestinationDecorator { destination ->
        val id = destination.instance.id
        DisposableEffect(id) {
            idsInComposition.add(id)
            onDispose {
                val notInComposition = idsInComposition.remove(id)
                val popped = id !in idsInBackstack
                if (popped && notInComposition) {
                    @Suppress("UNCHECKED_CAST")
                    (decoratorsToInvokeOnPop as List<NavigationDestinationDecorator<NavigationKey>>)
                        .distinct()
                        .asReversed()
                        .forEach { it.onPop(destination.instance) }
                }
            }
        }
        destination.Content()
    }
}
