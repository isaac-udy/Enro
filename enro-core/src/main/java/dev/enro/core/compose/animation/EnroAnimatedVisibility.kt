package dev.enro.core.compose.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import dev.enro.core.NavigationAnimation
import dev.enro.core.compose.localActivity

@OptIn(ExperimentalComposeUiApi::class, ExperimentalTransitionApi::class)
@Composable
internal fun EnroAnimatedVisibility(
    visibleState: MutableTransitionState<Boolean>,
    animations: NavigationAnimation,
    content: @Composable () -> Unit
) {
    val activity = localActivity
    val resourceAnimations = remember(animations) {
        animations.asResource(activity.theme)
    }
    val size = remember { mutableStateOf(IntSize(0, 0)) }

    val animationStateValues = getAnimationResourceState(visibleState, if(visibleState.targetState) resourceAnimations.enter else  resourceAnimations.exit, size.value)

    if(!animationStateValues.isActive && !visibleState.isIdle) {
        updateTransition(visibleState, "EnroAnimatedVisibility")
    }
    if(visibleState.targetState || animationStateValues.isActive) {
        Box(
            modifier = Modifier
                .onGloballyPositioned {
                    size.value = it.size
                }
                .graphicsLayer(
                    alpha = animationStateValues.alpha,
                    scaleX = animationStateValues.scaleX,
                    scaleY = animationStateValues.scaleY,
                    rotationX = animationStateValues.rotationX,
                    rotationY = animationStateValues.rotationY,
                    translationX = animationStateValues.translationX,
                    translationY = animationStateValues.translationY,
                    transformOrigin = animationStateValues.transformOrigin
                )
                .pointerInteropFilter { _ ->
                    !visibleState.targetState
                },
        ) {
            content()
        }
    }
}