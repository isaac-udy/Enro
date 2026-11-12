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
 * Wraps [content] in an `AnimatedVisibility` that is visible while the enclosing navigation
 * destination is visible, so [content] animates in and out with the destination's
 * transition. The destination's scope is read from
 * [LocalNavigationAnimatedVisibilityScopeOrNull], which `NavigationDisplay` provides.
 *
 * When no scope is provided (for example, a composable rendered outside a
 * `NavigationDisplay` in a snapshot test), [content] is rendered in an `AnimatedVisibility`
 * that is always visible.
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
