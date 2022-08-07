package dev.enro.extensions

import android.animation.AnimatorInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.animation.addListener

internal fun View.animate(
    animOrAnimator: Int,
    onAnimationStart: () -> Unit = {},
    onAnimationEnd: () -> Unit = {}
): Long {
    clearAnimation()
    if (animOrAnimator == 0) {
        onAnimationEnd()
        return 0
    }
    val isAnimation = runCatching { context.resources.getResourceTypeName(animOrAnimator) == "anim" }.getOrElse { false }
    val isAnimator = !isAnimation && runCatching { context.resources.getResourceTypeName(animOrAnimator) == "animator" }.getOrElse { false }

    when {
        isAnimator -> {
            val animator = AnimatorInflater.loadAnimator(context, animOrAnimator)
            animator.setTarget(this)
            animator.addListener(
                onStart = { onAnimationStart() },
                onEnd = { onAnimationEnd() }
            )
            animator.start()
            return animator.duration
        }
        isAnimation -> {
            val animation = AnimationUtils.loadAnimation(context, animOrAnimator)
            animation.setAnimationListener(object: Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {}
                override fun onAnimationStart(animation: Animation?) {
                    onAnimationStart()
                }
                override fun onAnimationEnd(animation: Animation?) {
                    onAnimationEnd()
                }
            })
            startAnimation(animation)
            return animation.duration
        }
        else -> {
            onAnimationEnd()
            return 0
        }
    }
}