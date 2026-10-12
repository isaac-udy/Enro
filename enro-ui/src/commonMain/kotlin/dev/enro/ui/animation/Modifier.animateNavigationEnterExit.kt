package dev.enro.ui.animation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import dev.enro.ui.LocalNavigationAnimatedVisibilityScopeOrNull

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
 * Reads [LocalNavigationAnimatedVisibilityScopeOrNull]. Normally that
 * local is provided by `NavigationDisplay`'s overlay renderer, so
 * in-app the modifier always finds a scope. When it isn't provided —
 * typical of Paparazzi / snapshot tests that render a composable in
 * isolation without an Enro container — the modifier degrades to a
 * no-op rather than crashing, so design-system surfaces (dialogs,
 * popups) can still be rendered standalone for documentation
 * snapshots.
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
