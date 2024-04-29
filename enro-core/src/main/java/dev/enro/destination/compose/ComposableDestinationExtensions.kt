package dev.enro.core.compose

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Transition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalInspectionMode
import dev.enro.animation.NavigationAnimation
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.core.navigationContext

/**
 * Causes the ComposableDestination's transition to immediately finish
 */
@AdvancedEnroApi
public fun ComposableDestination.finishTransition() {
    val transition = owner.transitionState
    if (!transition.isIdle) {
        owner.transitionState = MutableTransitionState(transition.targetState)
    }
}

/**
 * Gets the transition for the ComposableDestination
 */
@AdvancedEnroApi
public fun ComposableDestination.getTransition() : Transition<Boolean> {
    return owner.transition
}

/**
 * Gets the transition for the current navigationContext. This is only valid if the current context is a ComposableDestination,
 * and will otherwise throw an exception.
 */
@AdvancedEnroApi
public val navigationTransition: Transition<Boolean>
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
 */
@Composable
@AdvancedEnroApi
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
        destination.owner.animationOverride = NavigationAnimation.Composable(
            enter = enter,
            exit = exit,
        )
        onDispose {  }
    }
}