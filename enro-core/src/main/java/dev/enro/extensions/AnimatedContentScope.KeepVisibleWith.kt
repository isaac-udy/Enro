package dev.enro.extensions

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateInt
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key

/**
 * By default, an AnimatedVisibility composable created with Transition<T>.AnimatedVisibility will stop rendering child content
 * even while the parent transition is still active. In the case of dialogs, this can cause a Dialog that's doing a custom
 * animation from inside of an AnimatedVisibility to exit before the animation is completed.
 *
 * This method allows an AnimatedVisibilityScope to bind itself to some other transition, and remain active (and rendering child
 * content) while the other transition is running.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun <T> AnimatedVisibilityScope.KeepVisibleWith(
    otherTransition: Transition<T>
) {
    key(otherTransition.currentState, otherTransition.targetState, otherTransition.hashCode()) {
        if(otherTransition.totalDurationNanos > 0) {
            transition.animateInt(
                transitionSpec = {
                    tween((otherTransition.totalDurationNanos / 1000000).toInt())
                },
                label = "bindVisibilityToTransition",
                targetValueByState = { it.hashCode() }
            ).value
        }
    }
}