package dev.enro.ui.animation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import dev.enro.ui.LocalNavigationAnimatedVisibilityScopeOrNull

/**
 * Applies [enter] and [exit] to a composable through `animateEnterExit`, in the
 * [AnimatedVisibilityScope][androidx.compose.animation.AnimatedVisibilityScope] of the
 * enclosing navigation destination. [enter] and [exit] run with the destination's
 * transition, so part of a destination can animate differently from the destination.
 *
 * The scope is read from [LocalNavigationAnimatedVisibilityScopeOrNull], which
 * `NavigationDisplay` provides. When no scope is provided (for example, a composable
 * rendered outside a `NavigationDisplay` in a snapshot test), the modifier has no effect.
 */
public fun Modifier.animateNavigationEnterExit(
    enter: EnterTransition = EnterTransition.None,
    exit: ExitTransition = ExitTransition.None,
): Modifier = composed {
    val scope = LocalNavigationAnimatedVisibilityScopeOrNull.current
        ?: return@composed this@composed
    scope.run {
        this@composed.animateEnterExit(enter = enter, exit = exit)
    }
}
