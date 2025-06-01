@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
package dev.enro3.ui.animation

import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.TransitionState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

/**
 * For some reason, it appears that rememberTransition does not correctly work in Compose for Web,
 * and will call the "remember" function multiple times, even though the key is the same. This
 * appears to be a bug in Compose for Web, and not in Enro. This function is a workaround for that,
 * and is copied from the implementation of rememberTransition in the Compose libraries, but uses
 * the hashCode of the TransitionState as the key, rather than the TransitionState itself.
 *
 * Copied from androidx.compose.animation.core.rememberTransition
 */
@Composable
internal fun <T> rememberTransitionCompat(
    transitionState: TransitionState<T>,
    label: String? = null
): Transition<T> {
    // ! USING transitionState.hashCode() AS KEY TO AVOID BUG IN COMPOSE FOR WEB !
    val transition = remember(transitionState.hashCode()) {
        Transition(transitionState = transitionState, label)
    }
    if (transitionState is SeekableTransitionState) {
        LaunchedEffect(transitionState.currentState, transitionState.targetState) {
            transitionState.observeTotalDuration()
            transitionState.compositionContinuationMutex.withLock {
                transitionState.composedTargetState = transitionState.targetState
                transitionState.compositionContinuation?.resume(transitionState.targetState)
                transitionState.compositionContinuation = null
            }
        }
    } else {
        transition.animateTo(transitionState.targetState)
    }
    DisposableEffect(transition) {
        onDispose {
            // Clean up on the way out, to ensure the observers are not stuck in an in-between
            // state.
            transition.onDisposed()
        }
    }
    return transition
}