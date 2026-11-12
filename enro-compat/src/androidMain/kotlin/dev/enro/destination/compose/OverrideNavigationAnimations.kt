package dev.enro.destination.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode
import dev.enro.annotations.AdvancedEnroApi


@Composable
@AdvancedEnroApi
@Deprecated(
    message = "Use the OverrideNavigationAnimations function that takes a content block instead; this function does not work correctly in some situations",
    level = DeprecationLevel.ERROR,
)
public fun OverrideNavigationAnimations(
    enter: EnterTransition,
    exit: ExitTransition,
) {
    error("INVALID")
}

/**
 * Renders [content] in an AnimatedVisibility that applies [enter] and [exit] with the transition of the current
 * destination. [content] receives the AnimatedVisibilityScope, which can animate parts of the screen separately and
 * can be passed to shared element modifiers. In inspection mode, OverrideNavigationAnimations does not render [content].
 */
@Composable
@AdvancedEnroApi
public fun OverrideNavigationAnimations(
    enter: EnterTransition,
    exit: ExitTransition,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    // navigationTransition requires a navigation context, which inspection mode does not provide.
    val isInspection = LocalInspectionMode.current
    if (isInspection) return

    navigationTransition.AnimatedVisibility(
        visible = { it == EnterExitState.Visible },
        enter = enter,
        exit = exit,
    ) {
        content()
    }
}