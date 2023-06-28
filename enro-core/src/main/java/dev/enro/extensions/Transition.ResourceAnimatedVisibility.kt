package dev.enro.extensions

import android.animation.AnimatorInflater
import android.annotation.SuppressLint
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
import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
internal data class ResourceAnimationState(
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
) : Parcelable {

    companion object {
        fun forAnimationStart(targetVisibility: Boolean) = ResourceAnimationState(
            alpha = if (targetVisibility) 0.0f else 1.0f,
            isActive = true
        )

        fun forAnimationEnd(targetVisibility: Boolean) = ResourceAnimationState(
            alpha = if (targetVisibility) 1.0f else 0.0f,
            isActive = false
        )
    }

}

@OptIn(ExperimentalTransitionApi::class, ExperimentalComposeUiApi::class)
@Composable
internal fun <T> Transition<T>.ResourceAnimatedVisibility(
    visible: @Composable (T) -> Boolean,
    modifier: Modifier = Modifier,
    @AnimRes @AnimatorRes enter: Int,
    @AnimRes @AnimatorRes exit: Int,
    content: @Composable () -> Unit
) = BoxWithConstraints(modifier = modifier) {
    val transition = createChildTransition(
        label = "ResourceAnimatedVisibility",
        transformToChildState = visible,
    )

    val context = LocalContext.current
    val size = with(LocalDensity.current) { IntSize(maxWidth.roundToPx(), maxHeight.roundToPx()) }

    val animationId = remember(enter, exit, transition.targetState) {
        when (transition.targetState) {
            true -> enter
            false -> exit
        }
    }

    val isAnim = remember(animationId) {
        if (animationId == 0) return@remember false
        context.resources.getResourceTypeName(animationId) == "anim"
    }

    val isAnimator = remember(animationId) {
        if (animationId == 0) return@remember false
        context.resources.getResourceTypeName(animationId) == "animator"
    }

    val animationState by when {
        isAnim -> transition.animateAnimResource(animationId, size)
        isAnimator -> transition.animateAnimatorResource(animationId, size)
        else -> remember {
            derivedStateOf { ResourceAnimationState.forAnimationEnd(true) }
        }
    }
    if (transition.currentState || transition.targetState || isRunning) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    alpha = animationState.alpha
                    scaleX = animationState.scaleX
                    scaleY = animationState.scaleY
                    rotationX = animationState.rotationX
                    rotationY = animationState.rotationY
                    translationX = animationState.translationX
                    translationY = animationState.translationY
                    transformOrigin = animationState.transformOrigin
                }
                .pointerInteropFilter { _ ->
                    !transition.targetState
                },
        ) {
            content()
        }
    }
}

@Composable
private fun Transition<Boolean>.animateAnimResource(
    @AnimRes resourceId: Int,
    size: IntSize,
): State<ResourceAnimationState> {
    val context = LocalContext.current
    val state = remember(targetState, resourceId) {
        mutableStateOf(
            ResourceAnimationState.forAnimationStart(targetState)
        )
    }
    val isCompleted = targetState == currentState

    val anim = remember(resourceId, targetState) {
        AnimationUtils.loadAnimation(context, resourceId).apply {
            initialize(size.width, size.height, size.width, size.height)
        }
    }

    animateFloat(
        transitionSpec = { tween(anim.duration.toInt()) },
        label = "animateAnimResource",
        targetValueByState = { if (it) 1.0f else 0.0f },
    )

    LaunchedEffect(anim) {
        if (isCompleted) {
            state.value = ResourceAnimationState.forAnimationEnd(targetState)
            return@LaunchedEffect
        }
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
            state.value = ResourceAnimationState.forAnimationEnd(targetState)
        }
    }

    return state
}

@SuppressLint("ClickableViewAccessibility")
private class AnimatorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    override fun onTouchEvent(event: MotionEvent?): Boolean = false
    override fun performClick(): Boolean = false
}

@Composable
private fun Transition<Boolean>.animateAnimatorResource(
    @AnimatorRes resourceId: Int,
    size: IntSize,
): State<ResourceAnimationState> {
    val context = LocalContext.current
    val state = remember(targetState, resourceId) {
        mutableStateOf(
            ResourceAnimationState.forAnimationStart(targetState)
        )
    }
    val isCompleted = targetState == currentState

    val animator = remember(resourceId, targetState) {
        AnimatorInflater.loadAnimator(context, resourceId)
    }
    val durationForTransition = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> animator.totalDuration.toInt()
        else -> 1000
    }
    animateFloat(
        transitionSpec = { tween(durationForTransition) },
        label = "animateAnimatorResource"
    ) { if (it) 1.0f else 0.0f }

    LaunchedEffect(animator) {
        if (isCompleted) {
            state.value = ResourceAnimationState.forAnimationEnd(targetState)
            return@LaunchedEffect
        }
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
            state.value = ResourceAnimationState.forAnimationEnd(targetState)
        }
    }
    return state
}