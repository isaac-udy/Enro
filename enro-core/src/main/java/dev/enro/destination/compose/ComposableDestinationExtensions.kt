package dev.enro.core.compose

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Transition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.animation.NavigationAnimation
import dev.enro.core.navigationContext

@AdvancedEnroApi
public fun ComposableDestination.finishTransition() {
    val transition = owner.transitionState
    if (!transition.isIdle) {
        owner.transitionState = MutableTransitionState(transition.targetState)
    }
}

@AdvancedEnroApi
public fun ComposableDestination.getTransition() : Transition<Boolean> {
    return owner.transition
}

@AdvancedEnroApi
public val navigationTransition: Transition<Boolean>
    @Composable
    get() {
        val destination = navigationContext.contextReference as ComposableDestination
        return destination.getTransition()
    }

@Composable
@AdvancedEnroApi
public fun OverrideNavigationAnimations(
    enter: EnterTransition,
    exit: ExitTransition,
) {
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