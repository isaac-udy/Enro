package dev.enro.destination.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalInspectionMode
import dev.enro.animation.NavigationAnimation
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.destination.compose.destination.AnimationEvent

/**
 * Causes the ComposableDestination's transition to immediately finish
 * Android implementation
 */
@AdvancedEnroApi
public fun ComposableDestination.finishTransition() {
    owner.animations.setAnimationEvent(AnimationEvent.SnapTo(false))
}

/**
 * Gets the transition for the ComposableDestination
 * Android implementation
 */
@AdvancedEnroApi
public fun ComposableDestination.getTransition(): Transition<EnterExitState> {
    return owner.animations.enterExitTransition
}

/**
 * Gets the transition for the current navigationContext. This is only valid if the current context is a ComposableDestination,
 * and will otherwise throw an exception.
 */
@AdvancedEnroApi
public val navigationTransition: Transition<EnterExitState>
    @Composable
    get() {
        val destination = navigationContext.contextReference as ComposableDestination
        return destination.getTransition()
    }

/**
 * Overrides the navigation animations for the destination. This is primarily useful when the animations for a given destination
 * should not be applied from the navigation container. The cases for this are rare, and are generally edge case situations.
 *
 * For example, this is used in the DialogDestination Composable, because that Composable expects that a Dialog will handle it's
 * own entering and exiting animations, rather than attempting to animate the Composable that is holding the Dialog.
 *
 * Android implementation
 */
@Composable
@AdvancedEnroApi
@Deprecated("Use the OverrideNavigationAnimations function that takes a content block instead; this function does not work correctly in some situations")
public fun OverrideNavigationAnimations(
    enter: EnterTransition,
    exit: ExitTransition,
) {
    // If we are in inspection mode, we need to ignore this call, as it relies on items like navigationContext
    // which are only available in actual running applications
    val isInspection = LocalInspectionMode.current
    if (isInspection) return

    val navigationContext = navigationContext
    val destination = navigationContext.contextReference as ComposableDestination
    DisposableEffect(enter, exit) {
        destination.owner.animations.setAnimationOverride(NavigationAnimation.Composable(
            enter = enter,
            exit = exit,
        ))
        onDispose { }
    }
}

/**
 * Override the navigation animations for a particular destination, and also provide a content block that will be animated
 * using AnimatedVisibility, providing a AnimatedVisibilityScope which can be used to animate different parts of the screen
 * at different times, or to use in shared element transitions (when that is released in Compose).
 *
 * Android implementation
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

    val navigationContext = navigationContext
    val destination = navigationContext.contextReference as ComposableDestination

    var isOverrideSet by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        val overrideAnimation = NavigationAnimation.Composable(
            enter = fadeIn(
                initialAlpha = 0.99999f,
                animationSpec = snap(64),
            ),
            // We need a little fade out here to keep the animation active while the animated visibility below has a chance to run
            // and attach child transitions. This is a bit of a hack, but it's the only way to ensure that child exit transitions
            // are fully run.
            exit = fadeOut(
                targetAlpha = 0.99999f,
                animationSpec = snap(64),
            ),
        )
        destination.owner.animations.setAnimationOverride(overrideAnimation)
        isOverrideSet = true
        onDispose { }
    }

    if (!isOverrideSet) return
    navigationTransition.AnimatedVisibility(
        visible = { it == EnterExitState.Visible },
        enter = enter,
        exit = exit,
    ) {
        content()
    }
}