package dev.enro.extensions

import android.animation.AnimatorInflater
import android.content.Context
import android.graphics.Matrix
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.Transformation
import androidx.compose.animation.core.ExperimentalTransitionApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.delay
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@OptIn(ExperimentalComposeUiApi::class, ExperimentalTransitionApi::class)
@Composable
internal fun AnimatedVisibilityFromResources(
    visibleState: MutableTransitionState<Boolean>,
    enterAnimation: Int,
    exitAnimation: Int,
    content: @Composable () -> Unit
) {
    val size = remember { mutableStateOf(IntSize(0, 0)) }

    val animationStateValues = getAnimationResourceState(visibleState, if(visibleState.targetState) enterAnimation else  exitAnimation, size.value)

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

private class AnimatorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return false
    }
}

@Parcelize
internal data class AnimationResourceState(
    val alpha: Float = 1.0f,
    val scaleX: Float = 1.0f,
    val scaleY: Float = 1.0f,
    val translationX: Float = 0.0f,
    val translationY: Float = 0.0f,
    val rotationX: Float = 0.0f,
    val rotationY: Float = 0.0f,
    val transformOrigin: @RawValue TransformOrigin = TransformOrigin.Center,

    val playTime: Long = 0,
    val isActive: Boolean = false
) : Parcelable

@Composable
internal fun getAnimationResourceState(
    transitionState: MutableTransitionState<Boolean>,
    animOrAnimator: Int,
    size: IntSize
): AnimationResourceState {
    val state =
        remember(animOrAnimator) { mutableStateOf(AnimationResourceState(isActive = animOrAnimator != 0)) }
    if (transitionState.isIdle) return AnimationResourceState(isActive = false)
    if (animOrAnimator == 0) return state.value

    updateAnimationResourceStateFromAnim(transitionState, state, animOrAnimator, size)
    updateAnimationResourceStateFromAnimator(transitionState, state, animOrAnimator, size)

    LaunchedEffect(animOrAnimator) {
        val start = System.currentTimeMillis()
        while (state.value.isActive) {
            state.value = state.value.copy(playTime = System.currentTimeMillis() - start)
            delay(8)
        }
    }
    return state.value
}

@Composable
private fun updateAnimationResourceStateFromAnim(
    transitionState: MutableTransitionState<Boolean>,
    state: MutableState<AnimationResourceState>,
    animOrAnimator: Int,
    size: IntSize
) {
    val context = LocalContext.current
    val isAnim =
        remember(animOrAnimator) { context.resources.getResourceTypeName(animOrAnimator) == "anim" }
    if (!isAnim) return
    if(size.width == 0 && size.height == 0) {
        state.value = AnimationResourceState(
            alpha = if(transitionState.currentState) 1f else 0f,
            isActive = true
        )
        return
    }

    val anim = remember(animOrAnimator, size) {
        AnimationUtils.loadAnimation(context, animOrAnimator).apply {
            initialize(
                size.width,
                size.height,
                size.width,
                size.height
            )
        }
    }
    val transformation = Transformation()
    anim.getTransformation(System.currentTimeMillis(), transformation)

    val v = FloatArray(9)
    transformation.matrix.getValues(v)
    state.value = AnimationResourceState(
        alpha = transformation.alpha,
        scaleX = v[Matrix.MSCALE_X],
        scaleY = v[Matrix.MSCALE_Y],
        translationX = v[Matrix.MTRANS_X],
        translationY = v[Matrix.MTRANS_Y],
        rotationX = 0.0f,
        rotationY = 0.0f,
        transformOrigin = TransformOrigin(0f, 0f),

        isActive = state.value.isActive && state.value.playTime < anim.duration,
        playTime = state.value.playTime,
    )
}

@Composable
private fun updateAnimationResourceStateFromAnimator(
    transitionState: MutableTransitionState<Boolean>,
    state: MutableState<AnimationResourceState>,
    animOrAnimator: Int,
    size: IntSize
) {
    val context = LocalContext.current
    val isAnimator =
        remember(animOrAnimator) { context.resources.getResourceTypeName(animOrAnimator) == "animator" }
    if (!isAnimator) return

    val animator = remember(animOrAnimator, size) {
        state.value = AnimationResourceState(
            alpha = if(transitionState.currentState) 1f else 0f,
            isActive = true
        )
        AnimatorInflater.loadAnimator(context, animOrAnimator)
    }
    val animatorView = remember(animOrAnimator, size) {
        AnimatorView(context).apply {
            layoutParams = ViewGroup.LayoutParams(size.width, size.height)
            animator.setTarget(this)
            animator.start()
        }
    }

    state.value = AnimationResourceState(
        alpha = animatorView.alpha,
        scaleX = animatorView.scaleX,
        scaleY = animatorView.scaleY,
        translationX = animatorView.translationX,
        translationY = animatorView.translationY,
        rotationX = animatorView.rotationX,
        rotationY = animatorView.rotationY,

        isActive = state.value.isActive && animator.isRunning,
        playTime = state.value.playTime
    )
}
