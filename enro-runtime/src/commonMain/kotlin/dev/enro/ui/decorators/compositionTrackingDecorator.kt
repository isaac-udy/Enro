package dev.enro.ui.decorators

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import dev.enro.NavigationKey

/**
 * The **innermost** [NavigationDestinationDecorator] in the chain.
 * Registers a [DisposableEffect] keyed on the destination instance id
 * that tracks composition presence in `idsInComposition`, and fires
 * `onPop` on every other decorator (in reverse decoration order) when
 * the destination is both no longer in the backstack and no longer in
 * composition.
 *
 * Pairs with `PrepareBackStack` in `rememberDecoratedDestinations`
 * (which catches the inverse case: entry leaves the backstack while
 * its composition was already gone).
 *
 * **Why this lives inside the chain rather than in
 * `decorateNavigationDestination`'s outer wrap** — see
 * `docs/NAV3-COMPARISON.md`. In short: because it's the innermost
 * decorator, this `DisposableEffect` is composed inside the
 * `movableContent` set up by [movableContentDecorator]. Its
 * `onDispose` therefore fires in the same slot-table-teardown pass as
 * the inner `CompositionLocalProvider`s (saved state, view-model
 * store, navigation context). Some Enro decorators' `onPop` callbacks
 * tear down state that those inner providers expose — clearing the
 * destination's child `ViewModelStore`, transitioning a `LifecycleRegistry`
 * to `DESTROYED`. If `onPop` fired from an outer `DisposableEffect`
 * that disposes EARLIER than the movable content's discard (which is
 * deferred via `disposeUnusedMovableContent`), a recompose could land
 * between "onPop fired" and "inner chain disposed" and read state
 * mid-teardown. Doing the tracking from the innermost decorator
 * guarantees that everything happens in one synchronous slot-table
 * teardown.
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
