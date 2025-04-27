package dev.enro.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.rememberTransition
import androidx.compose.runtime.Immutable

public interface NavigationAnimation {
    public interface Enter : NavigationAnimation
    public interface Exit : NavigationAnimation
    public object None : NavigationAnimation, Enter, Exit

    public sealed class Composable : Enter, Exit {
        @androidx.compose.runtime.Composable
        internal abstract fun Animate(
            state: SeekableTransitionState<Boolean>,
            isSeeking: Boolean,
            content: @androidx.compose.runtime.Composable (Transition<EnterExitState>) -> Unit,
        )

        public companion object {
            public val none: Composable = invoke(
                enter = EnterTransition.None,
                exit = ExitTransition.None,
            )

            public operator fun invoke(
                enter: EnterTransition,
                exit: ExitTransition,
            ): Composable = EnterExit(enter, exit)

            public operator fun invoke(
                enter: EnterTransition,
            ): Enter = EnterExit(enter, ExitTransition.None)

            public operator fun invoke(
                exit: ExitTransition,
            ): Exit = EnterExit(EnterTransition.None, exit)
        }

        @Immutable
        internal data class EnterExit(
            val enter: EnterTransition = EnterTransition.None,
            val exit: ExitTransition = ExitTransition.None,
        ) : Composable(), Enter, Exit {
            @androidx.compose.runtime.Composable
            override fun Animate(
                state: SeekableTransitionState<Boolean>,
                isSeeking: Boolean,
                content: @androidx.compose.runtime.Composable (Transition<EnterExitState>) -> Unit,
            ) {
                val visible = rememberTransition(state, "ComposableDestination Visibility")
                visible.AnimatedVisibility(
                    visible = { it },
                    enter = enter,
                    exit = exit,
                ) {
                    content(transition)
                }
            }
        }
    }
}

