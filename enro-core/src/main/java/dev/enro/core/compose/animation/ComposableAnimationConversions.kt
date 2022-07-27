package dev.enro.core.compose.animation

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
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.delay
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

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
    animOrAnimator: Int,
    size: IntSize
): AnimationResourceState {
    val state =
        remember(animOrAnimator) { mutableStateOf(AnimationResourceState(isActive = true)) }
    if (animOrAnimator == 0) return state.value

    updateAnimationResourceStateFromAnim(state, animOrAnimator, size)
    updateAnimationResourceStateFromAnimator(state, animOrAnimator, size)

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
            alpha = 0f,
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
            alpha = 0.0f,
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
