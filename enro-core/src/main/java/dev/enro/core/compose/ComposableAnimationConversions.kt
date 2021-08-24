package dev.enro.core.compose

import android.animation.AnimatorInflater
import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.Transformation
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import kotlinx.parcelize.Parcelize

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
    val transformOrigin: TransformOrigin = TransformOrigin.Center,

    val playTime: Long = 0,
    val isActive: Boolean = false
) : Parcelable

@Composable
internal fun getAnimationResourceState(animOrAnimator: Int): AnimationResourceState {
    val state =
        remember(animOrAnimator) { mutableStateOf(AnimationResourceState(isActive = true)) }
    if (animOrAnimator == 0) return state.value

    updateAnimationResourceStateFromAnim(state, animOrAnimator)
    updateAnimationResourceStateFromAnimator(state, animOrAnimator)

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
    animOrAnimator: Int
) {
    val context = LocalContext.current
    val isAnim =
        remember(animOrAnimator) { context.resources.getResourceTypeName(animOrAnimator) == "anim" }
    if (!isAnim) return

    val containerState = LocalEnroContainerState.current
    val anim = remember(animOrAnimator) {
        AnimationUtils.loadAnimation(context, animOrAnimator).apply {
            initialize(
                containerState.size.width.toInt(),
                containerState.size.height.toInt(),
                containerState.size.width.toInt(),
                containerState.size.height.toInt()
            )
        }
    }
    val transformation = Transformation()
    anim.getTransformation(System.currentTimeMillis(), transformation)

    val v = FloatArray(9)
    transformation.matrix.getValues(v)
    state.value = AnimationResourceState(
        alpha = transformation.alpha,
        scaleX = v[android.graphics.Matrix.MSCALE_X],
        scaleY = v[android.graphics.Matrix.MSCALE_Y],
        translationX = v[android.graphics.Matrix.MTRANS_X],
        translationY = v[android.graphics.Matrix.MTRANS_Y],
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
    animOrAnimator: Int
) {
    val context = LocalContext.current
    val isAnimator =
        remember(animOrAnimator) { context.resources.getResourceTypeName(animOrAnimator) == "animator" }
    if (!isAnimator) return

    val animator = remember(animOrAnimator) {
        state.value = AnimationResourceState(
            alpha = 0.0f,
            isActive = true
        )
        AnimatorInflater.loadAnimator(context, animOrAnimator)
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            AnimatorView(it).apply {
                animator.setTarget(this)
                animator.start()
                animation
            }
        },
        update = {
            state.value = AnimationResourceState(
                alpha = it.alpha,
                scaleX = it.scaleX,
                scaleY = it.scaleY,
                translationX = it.translationX,
                translationY = it.translationY,
                rotationX = it.rotationX,
                rotationY = it.rotationY,

                isActive = state.value.isActive && animator.isRunning,
                playTime = state.value.playTime
            )
        }
    )
}