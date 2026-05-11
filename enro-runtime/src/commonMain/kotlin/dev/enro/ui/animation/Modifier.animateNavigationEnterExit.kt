package dev.enro.ui.animation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import dev.enro.ui.LocalNavigationAnimatedVisibilityScope

/**
 * Attaches enter / exit transitions to a composable, tied to the
 * surrounding navigation destination's [AnimatedVisibilityScope][androidx.compose.animation.AnimatedVisibilityScope].
 *
 * Use this from inside a navigation destination when a particular
 * piece of its content needs its own staggered animation alongside
 * the destination-level transition (for example, fading the scrim
 * at a different rate than the card it sits behind). The destination
 * itself drives the visibility flip; this modifier just plumbs the
 * caller's transitions into the active scope.
 *
 * Reads [LocalNavigationAnimatedVisibilityScope] — must be called
 * from inside a destination that's rendered by `NavigationDisplay`
 * (which is everywhere destinations live, in practice).
 */
public fun Modifier.animateNavigationEnterExit(
    enter: EnterTransition = EnterTransition.None,
    exit: ExitTransition = ExitTransition.None,
): Modifier = composed {
    val scope = LocalNavigationAnimatedVisibilityScope.current
    scope.run {
        this@composed.animateEnterExit(enter = enter, exit = exit)
    }
}
