package dev.enro.core.compose.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import dev.enro.core.AnimationPair
import dev.enro.core.compose.localActivity

@OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
@Composable
internal fun EnroAnimatedVisibility(
    visible: Boolean,
    animations: AnimationPair,
    content: @Composable () -> Unit
) {
    val context = localActivity
    val resourceAnimations = remember(animations) {
        animations.asResource(context.theme)
    }

    val size = remember { mutableStateOf(IntSize(0, 0)) }
    val animationStateValues = getAnimationResourceState(if(visible) resourceAnimations.enter else resourceAnimations.exit, size.value)
    val currentVisibility = remember {
        mutableStateOf(false)
    }
    AnimatedVisibility(
        modifier = Modifier
            .onGloballyPositioned {
                size.value = it.size
            },
        visible = currentVisibility.value || animationStateValues.isActive,
        enter = fadeIn(
            animationSpec = tween(1),
            initialAlpha = 1.0f
        ),
        exit = fadeOut(
            animationSpec = tween(1),
            targetAlpha = 1.0f
        ),
    ) {
        Box(
            modifier = Modifier
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
    SideEffect {
        currentVisibility.value = visible
    }
}
