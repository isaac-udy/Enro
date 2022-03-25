package dev.enro.core.compose.dialog

import android.animation.AnimatorInflater
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import dagger.hilt.android.AndroidEntryPoint
import dev.enro.core.*
import kotlinx.parcelize.Parcelize
import android.graphics.drawable.ColorDrawable
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.graphics.lerp
import androidx.core.animation.addListener
import androidx.core.view.isVisible
import dev.enro.core.compose.*
import dev.enro.core.container.EmptyBehavior
import java.lang.IllegalStateException


internal abstract class AbstractComposeDialogFragmentHostKey : NavigationKey {
    abstract val instruction: NavigationInstruction.Open
}

@Parcelize
internal data class ComposeDialogFragmentHostKey(
    override val instruction: NavigationInstruction.Open
) : AbstractComposeDialogFragmentHostKey()

@Parcelize
internal data class HiltComposeDialogFragmentHostKey(
    override val instruction: NavigationInstruction.Open
) : AbstractComposeDialogFragmentHostKey()


abstract class AbstractComposeDialogFragmentHost : DialogFragment() {
    private val navigationHandle by navigationHandle<AbstractComposeDialogFragmentHostKey>()

    private lateinit var dialogConfiguration: DialogConfiguration

    private val composeViewId = View.generateViewId()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(
            STYLE_NO_FRAME,
            requireActivity().packageManager.getActivityInfo(
                requireActivity().componentName,
                0
            ).themeResource
        )
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (dialog is Dialog) {
            dialog.setOnKeyListener { _, _, _ ->
                false
            }
        }
        super.onDismiss(dialog)
    }

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val composeView = ComposeView(requireContext()).apply {
            id = composeViewId
            setContent {
                val controller = rememberEnroContainerController(
                    initialBackstack = listOf(navigationHandle.key.instruction),
                    accept = { false },
                    emptyBehavior = EmptyBehavior.CloseParent
                )

                val destination = controller.getDestinationContext(navigationHandle.key.instruction).destination
                dialogConfiguration = when(destination) {
                    is BottomSheetDestination -> {
                        EnroBottomSheetContainer(controller, destination)
                        destination.bottomSheetConfiguration
                    }
                    is DialogDestination -> {
                        EnroDialogContainer(controller, destination)
                        destination.dialogConfiguration
                    }
                    else -> throw EnroException.DestinationIsNotDialogDestination("The @Composable destination for ${navigationHandle.key::class.java.simpleName} must be a DialogDestination or a BottomSheetDestination")
                }

                DisposableEffect(true) {
                    enter()
                    onDispose { }
                }
            }
        }

        return FrameLayout(requireContext()).apply {
            isVisible = false
            addView(composeView)
        }
    }

    private fun enter() {
        val activity = activity ?: return
        val dialogView = view ?: return
        val composeView = view?.findViewById<View>(composeViewId) ?: return

        dialogView.isVisible = true
        dialogView.clearAnimation()
        dialogView.animateToColor(dialogConfiguration.scrimColor)
        composeView.animate(
            dialogConfiguration.animations.asResource(activity.theme).enter,
        )
    }

    override fun dismiss() {
        val view = view ?: run {
            super.dismiss()
            return
        }
        val composeView = view.findViewById<View>(composeViewId) ?: run {
            super.dismiss()
            return
        }
        dialogConfiguration.isDismissed.value = true
        view.isVisible = true
        view.clearAnimation()
        view.animateToColor(Color.Transparent)
        composeView.animate(
            dialogConfiguration.animations.asResource(requireActivity().theme).exit,
            onAnimationEnd = {
                super.dismiss()
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog!!.apply {
            window!!.apply {
                setOnKeyListener { _, keyCode, event ->
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                        navigationContext.leafContext().getNavigationHandleViewModel()
                            .requestClose()
                        return@setOnKeyListener true
                    }
                    return@setOnKeyListener false
                }

                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                setBackgroundDrawableResource(android.R.color.transparent)
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }
        }
    }
}

class ComposeDialogFragmentHost : AbstractComposeDialogFragmentHost()

@AndroidEntryPoint
class HiltComposeDialogFragmentHost : AbstractComposeDialogFragmentHost()

internal fun View.animateToColor(color: Color) {
    val backgroundColorInt = if (background is ColorDrawable) (background as ColorDrawable).color else 0
    val backgroundColor = Color(backgroundColorInt)

    animate()
        .setDuration(225)
        .setInterpolator(AccelerateDecelerateInterpolator())
        .setUpdateListener {
            setBackgroundColor(lerp(backgroundColor, color, it.animatedFraction).toArgb())
        }
        .start()
}

internal fun View.animate(
    animOrAnimator: Int,
    onAnimationEnd: () -> Unit = {}
) {
    clearAnimation()
    if (animOrAnimator == 0) {
        onAnimationEnd()
        return
    }
    val isAnimation = runCatching { context.resources.getResourceTypeName(animOrAnimator) == "anim" }.getOrElse { false }
    val isAnimator = !isAnimation && runCatching { context.resources.getResourceTypeName(animOrAnimator) == "animator" }.getOrElse { false }

    when {
        isAnimator -> {
            val animator = AnimatorInflater.loadAnimator(context, animOrAnimator)
            animator.setTarget(this)
            animator.addListener(
                onEnd = { onAnimationEnd() }
            )
            animator.start()
        }
        isAnimation -> {
            val animation = AnimationUtils.loadAnimation(context, animOrAnimator)
            animation.setAnimationListener(object: Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {}
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    onAnimationEnd()
                }
            })
            startAnimation(animation)
        }
        else -> {
            onAnimationEnd()
        }
    }
}