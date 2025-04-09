package dev.enro.destination.compose

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.updateTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.enro.annotations.AdvancedEnroApi

/**
 * Causes the ComposableDestination's transition to immediately finish
 * iOS implementation (placeholder)
 */
@AdvancedEnroApi
public actual fun ComposableDestination.finishTransition() {
    // Placeholder implementation for iOS
}

/**
 * Gets the transition for the ComposableDestination
 * iOS implementation (placeholder)
 */
@AdvancedEnroApi
public actual fun ComposableDestination.getTransition(): Transition<EnterExitState> {
    // Placeholder implementation returning a dummy transition
    val state = MutableTransitionState(EnterExitState.Visible)
    return updateTransition(state, label = "ComposableDestinationTransition")
}

/**
 * Overrides the navigation animations for the destination.
 * iOS implementation (placeholder)
 */
@Composable
@AdvancedEnroApi
@Deprecated("Use the OverrideNavigationAnimations function that takes a content block instead; this function does not work correctly in some situations")
public actual fun OverrideNavigationAnimations(
    enter: EnterTransition,
    exit: ExitTransition,
) {
    // Placeholder implementation for iOS
}

/**
 * Override the navigation animations for a particular destination
 * iOS implementation (placeholder)
 */
@Composable
@AdvancedEnroApi
public actual fun OverrideNavigationAnimations(
    enter: EnterTransition,
    exit: ExitTransition,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    // Placeholder implementation - just display the content
    content()
}