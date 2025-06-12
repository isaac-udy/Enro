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
 * Override the navigation animations for a particular destination, and also provide a content block that will be animated
 * using AnimatedVisibility, providing a AnimatedVisibilityScope which can be used to animate different parts of the screen
 * at different times, or to use in shared element transitions (when that is released in Compose).
 */
@Composable
@AdvancedEnroApi
public fun OverrideNavigationAnimations(
    enter: EnterTransition,
    exit: ExitTransition,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    // If we are in inspection mode, we need to ignore this call, as it relies on items like navigationContext
    // which are only available in actual running applications
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