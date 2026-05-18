package dev.enro.ui.decorators

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.enro.NavigationKey
import dev.enro.ui.LocalNavigationAnimatedVisibilityScopeOrNull
import dev.enro.ui.LocalNavigationSharedTransitionScopeOrNull

/**
 * Returns a [NavigationDestinationDecorator] that wraps each
 * destination's content in `Box(Modifier.sharedElement(...))` keyed by
 * the destination instance id.
 *
 * **Why this exists.** Without it, when a scene transition mounts the
 * same destination in two different scene compositions (e.g. going
 * from a single-pane scene to a two-pane scene that still contains the
 * previous entry, or swapping the top entry of a two-pane scene), the
 * AnimatedContent in `NavigationDisplay` slides whole scene
 * compositions in and out — and the shared entry rides along with the
 * incoming composition. Wrapping every entry in a `sharedElement` Box
 * lets Compose's `SharedTransitionScope` bridge the entry's bounds
 * from its old layout slot to its new one, so it visually stays put
 * (or smoothly transitions) instead of re-animating with the scene.
 *
 * **Crucial detail — this decorator must sit OUTSIDE the
 * exclusion/movable-content gate.** The
 * [movableContentDecorator] short-circuits and emits nothing when the
 * destination isn't supposed to render in the current scene (because
 * another scene won it). For the `sharedElement` bridge to work,
 * Compose needs to see a layout node with the matching key in BOTH
 * scene compositions during the transition. So we always emit the
 * `Box(Modifier.sharedElement(...))`; the `movableContentDecorator`
 * inside decides whether to render the actual content or leave the
 * Box empty. An empty Box still participates in shared-element bounds
 * tracking via the scene's outer layout placement.
 *
 * Apply this as the **first** decorator in the chain — `foldRight`
 * makes the first decorator the outermost wrapper.
 *
 * If no `SharedTransitionScope` or `AnimatedVisibilityScope` is
 * available (e.g. a destination rendered standalone for previews or
 * snapshot tests, outside any `NavigationDisplay`), this decorator
 * gracefully degrades to just calling `destination.content()`.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
public fun rememberSharedElementDecorator(): NavigationDestinationDecorator<NavigationKey> =
    remember {
        sharedElementDecorator()
    }

@OptIn(ExperimentalSharedTransitionApi::class)
internal fun sharedElementDecorator(): NavigationDestinationDecorator<NavigationKey> =
    navigationDestinationDecorator { destination ->
        val sharedTransitionScope = LocalNavigationSharedTransitionScopeOrNull.current
        val animatedVisibilityScope = LocalNavigationAnimatedVisibilityScopeOrNull.current
        if (sharedTransitionScope == null || animatedVisibilityScope == null) {
            destination.content()
            return@navigationDestinationDecorator
        }
        with(sharedTransitionScope) {
            Box(
                modifier = Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState(destination.instance.id),
                    animatedVisibilityScope = animatedVisibilityScope,
                ),
            ) {
                destination.content()
            }
        }
    }
