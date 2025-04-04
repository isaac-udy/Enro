package dev.enro.animation

import android.content.Context
import android.content.res.Resources
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.rememberTransition
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dev.enro.core.EnroConfig
import dev.enro.core.controller.NavigationApplication
import dev.enro.extensions.ResourceAnimatedVisibility
import dev.enro.extensions.getAttributeResourceId

public sealed interface NavigationAnimation {
    public sealed interface Enter : NavigationAnimation
    public sealed interface Exit : NavigationAnimation
    public sealed interface ForView : NavigationAnimation, Enter, Exit

    public data class Resource(
        public val id: Int
    ) : ForView, Enter, Exit {
        public fun isAnim(context: Context): Boolean = runCatching {
            if (id == 0) return@runCatching false
            context.resources.getResourceTypeName(id) == "anim"
        }.getOrDefault(false)

        public fun isAnimator(context: Context): Boolean = runCatching {
            if (id == 0) return@runCatching false
            context.resources.getResourceTypeName(id) == "animator"
        }.getOrDefault(false)
    }

    public data class Attr(
        public val attr: Int,
    ) : ForView, Enter, Exit

    public data class Theme(
        public val id: (Resources.Theme) -> Int,
    ) : ForView, Enter, Exit

    public sealed class Composable : NavigationAnimation, Enter, Exit {
        internal abstract val forView: ForView

        @androidx.compose.runtime.Composable
        internal abstract fun Animate(
            state: SeekableTransitionState<Boolean>,
            isSeeking: Boolean,
            content: @androidx.compose.runtime.Composable (Transition<EnterExitState>) -> Unit,
        )

        public companion object {
            public operator fun invoke(
                enter: EnterTransition,
                exit: ExitTransition,
                forView: ForView = DefaultAnimations.ForView.noneEnter,
            ): Composable = EnterExit(enter, exit, forView)

            public operator fun invoke(
                enter: EnterTransition,
                forView: ForView = DefaultAnimations.ForView.noneEnter,
            ): Enter = EnterExit(enter, ExitTransition.None, forView)

            public operator fun invoke(
                exit: ExitTransition,
                forView: ForView = DefaultAnimations.ForView.noneCloseExit,
            ): Exit = EnterExit(EnterTransition.None, exit, forView)

            public operator fun invoke(
                forView: ForView,
            ): Composable = EnterExit(forView = forView)
        }

        @Immutable
        internal data class EnterExit(
            val enter: EnterTransition = EnterTransition.None,
            val exit: ExitTransition = ExitTransition.None,
            override val forView: ForView = DefaultAnimations.ForView.noneEnter,
        ) : Composable(), Enter, Exit {
            @androidx.compose.runtime.Composable
            override fun Animate(
                state: SeekableTransitionState<Boolean>,
                isSeeking: Boolean,
                content: @androidx.compose.runtime.Composable (Transition<EnterExitState>) -> Unit,
            ) {
                val visible = rememberTransition(state, "ComposableDestination Visibility")
                val context = LocalContext.current
                val config = remember(context) {
                    val navigationApplication = (context.applicationContext as? NavigationApplication)
                    navigationApplication?.navigationController?.config ?: EnroConfig()
                }

                val resourceAnimation = remember(this, forView) { forView.asResource(context.theme) }
                visible.AnimatedVisibility(
                    visible = { it },
                    enter = enter,
                    exit = exit,
                ) {
                    if (config.enableViewAnimationsForCompose) {
                        transition.ResourceAnimatedVisibility(
                            visible = { it == EnterExitState.Visible },
                            enter = resourceAnimation.id,
                            exit = resourceAnimation.id,
                            progress = state.fraction,
                            isSeeking = isSeeking,
                        ) {
                            content(transition)
                        }
                    } else {
                        content(transition)
                    }
                }
            }
        }
    }

    public fun asResource(theme: Resources.Theme): Resource = when (this) {
        is Resource -> this
        is Attr -> Resource(
            theme.getAttributeResourceId(attr),
        )

        is Theme -> Resource(
            id(theme),
        )

        is Composable -> forView.asResource(theme)
    }

    public fun asComposable(): Composable {
        return when (this) {
            is ForView -> Composable(forView = this)
            is Composable -> this
        }
    }
}

