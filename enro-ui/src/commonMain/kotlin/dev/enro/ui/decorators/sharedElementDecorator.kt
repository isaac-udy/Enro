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
 * Returns a [NavigationDestinationDecorator] that wraps each destination's content in
 * `Box(Modifier.sharedElement(...))`, keyed by the destination instance id.
 *
 * When a scene transition composes the same destination in two scenes, the `sharedElement`
 * Box animates the destination's bounds from the old scene to the new scene. Without the
 * Box, the destination animates with the incoming scene.
 *
 * The decorator must be the first decorator in the chain, which makes the decorator the
 * outermost wrapper. `movableContentDecorator` emits no content for a destination that
 * another scene renders, and the shared element transition requires the `sharedElement`
 * Box in both scenes.
 *
 * When no `SharedTransitionScope` or `AnimatedVisibilityScope` is provided (for example, a
 * destination rendered outside a `NavigationDisplay` in a preview or snapshot test), the
 * decorator calls `destination.Content()` without the Box.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
public fun rememberSharedElementDecorator(): NavigationDestinationDecorator<NavigationKey> =
    remember {
        sharedElementDecorator()
    }

internal fun sharedElementDecorator(): NavigationDestinationDecorator<NavigationKey> =
    navigationDestinationDecorator { destination ->
        SharedElementDestinationWrapper.Wrap(destination)
    }

/**
 * The [NavigationDestinationWrapper] that [dev.enro.ui.NavigationDisplay] provides.
 * SharedElementDestinationWrapper applies the `sharedElement` Box described on
 * [rememberSharedElementDecorator].
 */
@OptIn(ExperimentalSharedTransitionApi::class)
internal val SharedElementDestinationWrapper: NavigationDestinationWrapper =
    NavigationDestinationWrapper { destination ->
        val sharedTransitionScope = LocalNavigationSharedTransitionScopeOrNull.current
        val animatedVisibilityScope = LocalNavigationAnimatedVisibilityScopeOrNull.current
        if (sharedTransitionScope == null || animatedVisibilityScope == null) {
            destination.Content()
            return@NavigationDestinationWrapper
        }
        with(sharedTransitionScope) {
            Box(
                modifier = Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState(destination.instance.id),
                    animatedVisibilityScope = animatedVisibilityScope,
                ),
            ) {
                destination.Content()
            }
        }
    }
