package dev.enro.core.compose

import androidx.compose.animation.*
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import dev.enro.core.AnimationPair

@OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
@Composable
internal fun EnroAnimatedVisibility(
    visible: Boolean,
    animations: AnimationPair,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current as FragmentActivity
    val resourceAnimations = remember(animations) {
        animations.asResource(context.theme)
    }

    val animationStateValues = getAnimationResourceValues(resourceAnimations)
    val currentVisibility = remember {
        mutableStateOf(false)
    }
    AnimatedVisibility(
        modifier = Modifier
            .fillMaxSize(),
        visible = currentVisibility.value,
        enter = fadeIn(1.0f, tween(animationStateValues.first.duration.toInt())),
        exit = fadeOut(1.0f, tween(animationStateValues.second.duration.toInt())),
    ) {
        val spec: @Composable Transition.Segment<EnterExitState>.() -> FiniteAnimationSpec<Float> = {
            val duration = when(targetState) {
                EnterExitState.PreEnter -> animationStateValues.first.duration.toInt()
                EnterExitState.Visible -> animationStateValues.first.duration.toInt()
                EnterExitState.PostExit -> animationStateValues.first.duration.toInt()
            }
            tween(duration)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    alpha = transition.animateFloat (
                        transitionSpec = spec,
                        label = "alpha",
                        targetValueByState = {
                            when(it) {
                                EnterExitState.PreEnter -> animationStateValues.first.alpha
                                EnterExitState.Visible -> 1.0f
                                EnterExitState.PostExit -> animationStateValues.second.alpha
                            }
                        }
                    ).value,
                    scaleX = transition.animateFloat (
                        transitionSpec = spec,
                        label = "scaleX",
                        targetValueByState = {
                            when(it) {
                                EnterExitState.PreEnter -> animationStateValues.first.scaleX
                                EnterExitState.Visible -> 1.0f
                                EnterExitState.PostExit -> animationStateValues.second.scaleX
                            }
                        }
                    ).value,
                    scaleY = transition.animateFloat (
                        transitionSpec = spec,
                        label = "scaleY",
                        targetValueByState = {
                            when(it) {
                                EnterExitState.PreEnter -> animationStateValues.first.scaleY
                                EnterExitState.Visible -> 1.0f
                                EnterExitState.PostExit -> animationStateValues.second.scaleY
                            }
                        }
                    ).value,
                    rotationX = 0f,
                    rotationY = 0f,
                    translationX = transition.animateFloat (
                        transitionSpec = spec,
                        label = "translationX",
                        targetValueByState = {
                            when(it) {
                                EnterExitState.PreEnter -> animationStateValues.first.translationX
                                EnterExitState.Visible -> 1.0f
                                EnterExitState.PostExit -> animationStateValues.second.translationX
                            }
                        }
                    ).value,
                    translationY = transition.animateFloat (
                        transitionSpec = spec,
                        label = "translationY",
                        targetValueByState = {
                            when(it) {
                                EnterExitState.PreEnter -> animationStateValues.first.translationY
                                EnterExitState.Visible -> 1.0f
                                EnterExitState.PostExit -> animationStateValues.second.translationY
                            }
                        }
                    ).value,
                )
                .pointerInteropFilter { _ ->
                    !visible
                }
        ) {
            content()
        }
    }
    SideEffect {
        currentVisibility.value = visible
    }
}