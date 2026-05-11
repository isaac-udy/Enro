package dev.enro.ui.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.enro.ui.LocalNavigationAnimatedVisibilityScopeOrNull

/**
 * Wraps [content] in an `AnimatedVisibility` whose visibility is tied
 * to the surrounding navigation destination's
 * [AnimatedVisibilityScope][LocalNavigationAnimatedVisibilityScopeOrNull].
 *
 * When the scope is provided (the normal in-app case under
 * `NavigationDisplay`), the AnimatedVisibility's `visible` flag is
 * driven by the destination's transition — content animates in/out
 * alongside the surrounding scene.
 *
 * When the scope isn't provided (e.g. Paparazzi snapshot tests
 * rendering a piece standalone), [content] is rendered directly in a
 * [Box] applying [modifier] — there's nothing to animate against, so
 * we just show it. Without this fallback design-system snapshots for
 * any composable that uses this wrapper would crash.
 */
@Composable
public fun NavigationAnimatedVisibility(
    modifier: Modifier = Modifier,
    enter: EnterTransition = fadeIn() + expandIn(),
    exit: ExitTransition = shrinkOut() + fadeOut(),
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    val scope = LocalNavigationAnimatedVisibilityScopeOrNull.current
    if (scope == null) {
        // Snapshot / standalone path — render the content directly,
        // anchoring to the same `AnimatedVisibilityScope` contract by
        // delegating to a self-contained `AnimatedVisibility(true)`.
        AnimatedVisibility(
            visible = true,
            modifier = modifier,
            enter = enter,
            exit = exit,
            content = content,
        )
        return
    }
    scope.transition.AnimatedVisibility(
        visible = { it == EnterExitState.Visible },
        modifier = modifier,
        enter = enter,
        exit = exit,
        content = content,
    )
}
