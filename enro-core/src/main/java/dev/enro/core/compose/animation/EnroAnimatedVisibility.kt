package dev.enro.core.compose.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import dev.enro.core.NavigationAnimation
import dev.enro.core.compose.localActivity

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun EnroAnimatedVisibility(
    visibleState: Transition<Boolean>,
    animations: NavigationAnimation,
    content: @Composable () -> Unit
) = BoxWithConstraints {
    val activity = localActivity
    val resourceAnimations = remember(animations) {
        animations.asResource(activity.theme)
    }
    val size = with(LocalDensity.current) {
        IntSize(maxWidth.roundToPx(), maxHeight.roundToPx())
    }

    val animationStateValues = getAnimationResourceState(visibleState, resourceAnimations.id, size)
    if(visibleState.currentState || visibleState.targetState || animationStateValues.isActive) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    alpha = animationStateValues.alpha
                    scaleX = animationStateValues.scaleX
                    scaleY = animationStateValues.scaleY
                    rotationX = animationStateValues.rotationX
                    rotationY = animationStateValues.rotationY
                    translationX = animationStateValues.translationX
                    translationY = animationStateValues.translationY
                    transformOrigin = animationStateValues.transformOrigin
                }
                .pointerInteropFilter { _ ->
                    !visibleState.targetState
                },
        ) {
            content()
        }
    }
}