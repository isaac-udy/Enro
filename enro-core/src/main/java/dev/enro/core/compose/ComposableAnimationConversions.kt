package dev.enro.core.compose

import android.animation.AnimatorInflater
import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.Transformation
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import dev.enro.core.AnimationPair

internal data class AnimationResourceValues(
    val alpha: Float = 1.0f,
    val scaleX: Float = 1.0f,
    val scaleY: Float = 1.0f,
    val translationX: Float = 0.0f,
    val translationY: Float = 0.0f,
    val duration: Long = 1
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun getAnimationResourceValues(animation: AnimationPair.Resource): Pair<AnimationResourceValues, AnimationResourceValues> {
    val enterValues = getTargetAnimationStateFromAnim(animation.enter, true)
        ?: getTargetAnimationStateFromAnimator(animation.enter, true)
        ?: AnimationResourceValues()

    val exitValues = getTargetAnimationStateFromAnim(animation.exit, false)
        ?: getTargetAnimationStateFromAnimator(animation.exit, false)
        ?: AnimationResourceValues()

    val enter = fadeIn(
        initialAlpha = enterValues.alpha,
        animationSpec = tween(enterValues.duration.toInt())
    ) + expandIn(
        expandFrom = Alignment.Center,
        animationSpec = tween(enterValues.duration.toInt()),
        initialSize = {
            IntSize(
                width = (it.width * enterValues.scaleX).toInt(),
                height = (it.height * enterValues.scaleY).toInt()
            ).also { Log.e("Enro", "slideIn initialSize: $it") }
        }
    ) + slideIn(
        animationSpec = tween(enterValues.duration.toInt()),
        initialOffset = {
            IntOffset(
                x = (it.width * enterValues.translationX).toInt(),
                y = (it.height * enterValues.translationY).toInt()
            ).also { Log.e("Enro", "slideIn initialOffset: $it") }
        }
    )

    val exit = fadeOut(
        targetAlpha = exitValues.alpha,
        animationSpec = tween(exitValues.duration.toInt())
    ) + shrinkOut(
        shrinkTowards = Alignment.Center,
        animationSpec = tween(exitValues.duration.toInt()),
        targetSize = {
            IntSize(
                width = (it.width * exitValues.scaleX).toInt(),
                height = (it.height * exitValues.scaleY).toInt()
            ).also { Log.e("Enro", "shrinkOut target: $it") }
        }
    ) + slideOut(
        animationSpec = tween(exitValues.duration.toInt()),
        targetOffset = {
            IntOffset(
                x = (it.width * exitValues.translationX).toInt(),
                y = (it.height * exitValues.translationY).toInt()
            ).also { Log.e("Enro", "slideOut target: $it") }
        }
    )

    return enterValues to exitValues
}

@Composable
private fun getTargetAnimationStateFromAnim(
    animOrAnimator: Int,
    isEnter: Boolean
) : AnimationResourceValues? {
    if(animOrAnimator == 0) return null
    val context = LocalContext.current
    val isAnim = remember(animOrAnimator) {
        context.resources.getResourceTypeName(animOrAnimator) == "anim"
    }
    if (!isAnim) return null

    return remember(animOrAnimator) {
        val anim = AnimationUtils.loadAnimation(context, animOrAnimator)

        val transformation = Transformation()
        anim.startTime = 0
        anim.getTransformation(if(isEnter) 0 else anim.duration, transformation)

        val v = FloatArray(9)
        transformation.matrix.getValues(v)

        return@remember AnimationResourceValues(
            alpha = transformation.alpha,
            scaleX = v[Matrix.MSCALE_X],
            scaleY = v[Matrix.MSCALE_Y],
            translationX = v[Matrix.MTRANS_X],
            translationY = v[Matrix.MTRANS_Y],
            duration = anim.duration
        )
    }
}

@Composable
private fun getTargetAnimationStateFromAnimator(
    animOrAnimator: Int,
    isEnter: Boolean
): AnimationResourceValues? {
    if(animOrAnimator == 0) return null
    val context = LocalContext.current
    val isAnimator = remember(animOrAnimator) {
        context.resources.getResourceTypeName(animOrAnimator) == "animator"
    }
    if (!isAnimator) return null

    val state = remember { mutableStateOf(AnimationResourceValues()) }

    val animator = remember(animOrAnimator) {
        AnimatorInflater.loadAnimator(context, animOrAnimator)
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            AnimatorView(it).apply {
                animator.setTarget(this)
                if(isEnter) animator.setupStartValues() else animator.setupEndValues()
            }
        },
        update = {
            state.value = AnimationResourceValues(
                alpha = it.alpha,
                scaleX = it.scaleX,
                scaleY = it.scaleY,
                translationX = it.translationX,
                translationY = it.translationY,
                duration = animator.duration
            )
        }
    )
    return state.value
}

private class AnimatorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return false
    }
}