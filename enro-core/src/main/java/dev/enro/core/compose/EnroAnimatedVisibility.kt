package dev.enro.core.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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

    val animationStateValues = getAnimationResourceState(if(visible) resourceAnimations.enter else resourceAnimations.exit)
    val currentVisibility = remember {
        mutableStateOf(false)
    }
    AnimatedVisibility(
        modifier = Modifier
            .fillMaxSize(),
        visible = currentVisibility.value || animationStateValues.isActive,
        enter = fadeIn(1.0f, tween(1)),
        exit = fadeOut(1.0f, tween(1)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
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