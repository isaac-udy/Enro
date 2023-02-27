package dev.enro.core.compose.animation

import android.animation.AnimatorInflater
import android.content.Context
import android.graphics.Matrix
import android.os.Build
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.Transformation
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
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
    transitionState: Transition<Boolean>,
    animOrAnimator: Int,
    size: IntSize
): AnimationResourceState {
    val state = remember(animOrAnimator, transitionState.targetState) {
        mutableStateOf(AnimationResourceState(alpha = if (transitionState.targetState) 0.0f else 1.0f, isActive = true))
    }

    val context = LocalContext.current

    val isAnim = remember(animOrAnimator) {
        if (animOrAnimator == 0) return@remember false
        context.resources.getResourceTypeName(animOrAnimator) == "anim"
    }

    val isAnimator = remember(animOrAnimator) {
        if (animOrAnimator == 0) return@remember false
        context.resources.getResourceTypeName(animOrAnimator) == "animator"
    }

    when {
        isAnim && state.value.isActive -> rememberAnimationResourceStateFromAnim(
            transitionState,
            animOrAnimator,
            size,
            state
        )
        isAnimator && state.value.isActive -> rememberAnimationResourceStateFromAnimator(
            transitionState,
            animOrAnimator,
            size,
            state
        )
        else -> {
            state.value = state.value.copy(isActive = false, alpha = if (transitionState.currentState) 1.0f else 0.0f)
        }
    }
    return state.value
}

@Composable
private fun rememberAnimationResourceStateFromAnim(
    transitionState: Transition<Boolean>,
    animOrAnimator: Int,
    size: IntSize,
    state: MutableState<AnimationResourceState>
) {
    val context = LocalContext.current

    val anim = remember(animOrAnimator, transitionState.targetState) {
        AnimationUtils.loadAnimation(context, animOrAnimator).apply {
            initialize(
                size.width,
                size.height,
                size.width,
                size.height
            )
        }
    }

    // Add an animation on the TransitionState to ensure that the transition is still marked
    // as active while the anim's animation completes
    transitionState.animateFloat(
        transitionSpec = { tween(anim.duration.toInt()) },
        label = "Anim@$animOrAnimator"
    ) {
        if(it) 1.0f else 0.0f
    }

    LaunchedEffect(anim) {
        withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            val transformation = Transformation()
            val v = FloatArray(9)

            while (isActive && state.value.isActive) {
                anim.getTransformation(System.currentTimeMillis(), transformation)
                transformation.matrix.getValues(v)
                state.value = state.value.copy(
                    alpha = transformation.alpha,
                    scaleX = v[Matrix.MSCALE_X],
                    scaleY = v[Matrix.MSCALE_Y],
                    translationX = v[Matrix.MTRANS_X],
                    translationY = v[Matrix.MTRANS_Y],
                    rotationX = 0.0f,
                    rotationY = 0.0f,
                    transformOrigin = TransformOrigin(0f, 0f),
                    isActive = state.value.playTime < anim.duration,
                    playTime = System.currentTimeMillis() - startTime
                )
                delay(16)
            }
            state.value = state.value.copy(isActive = false)
        }
    }
}

@Composable
private fun rememberAnimationResourceStateFromAnimator(
    transitionState: Transition<Boolean>,
    animOrAnimator: Int,
    size: IntSize,
    state: MutableState<AnimationResourceState>
) {
    val context = LocalContext.current

    val animator = remember(animOrAnimator, transitionState.targetState) {
        AnimatorInflater.loadAnimator(context, animOrAnimator)
    }

    // Add an animation on the TransitionState to ensure that the transition is still marked
    // as active while the animator's animation completes
    transitionState.animateFloat(
        transitionSpec = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tween(animator.totalDuration.toInt())
            } else {
                tween(1000)
            }
        },
        label = "Animator@$animOrAnimator"
    ) {
        if(it) 1.0f else 0.0f
    }

    LaunchedEffect(animator) {
        val startTime = System.currentTimeMillis()
        val animatorView = AnimatorView(context).apply {
            layoutParams = ViewGroup.LayoutParams(size.width, size.height)
            animator.setTarget(this)
            animator.start()
        }
        withContext(Dispatchers.IO) {
            while (isActive && state.value.isActive) {
                state.value = state.value.copy(
                    alpha = animatorView.alpha,
                    scaleX = animatorView.scaleX,
                    scaleY = animatorView.scaleY,
                    translationX = animatorView.translationX,
                    translationY = animatorView.translationY,
                    rotationX = animatorView.rotationX,
                    rotationY = animatorView.rotationY,

                    isActive = animator.isRunning,
                    playTime = System.currentTimeMillis() - startTime
                )
                delay(16)
            }
        }
    }
}
