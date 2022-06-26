package dev.enro.core.compose.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.zIndex
import dev.enro.core.NavigationAnimation
import dev.enro.core.compose.localActivity

@OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
@Composable
internal fun EnroAnimatedVisibility(
    visible: Boolean,
    animations: NavigationAnimation.Resource,
    content: @Composable () -> Unit
) {
    val activity = localActivity
    val resourceAnimations = remember(animations) {
        animations.asResource(activity.theme)
    }
    val size = remember { mutableStateOf(IntSize(0, 0)) }

    val animationStateValues = getAnimationResourceState(if(visible) resourceAnimations.enter else resourceAnimations.exit, size.value)

    if(visible || animationStateValues.isActive) {
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
                    !visible
                }
        ) {
            content()
        }
    }
}