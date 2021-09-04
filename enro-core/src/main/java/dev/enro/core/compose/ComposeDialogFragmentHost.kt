package dev.enro.core.compose

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import dagger.hilt.android.AndroidEntryPoint
import dev.enro.core.*
import kotlinx.parcelize.Parcelize
import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.lerp
import kotlinx.coroutines.launch


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
        setStyle(STYLE_NO_FRAME, requireActivity().packageManager.getActivityInfo(requireActivity().componentName, 0).themeResource)
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
                    initialState = listOf(navigationHandle.key.instruction),
                    accept = { false },
                    emptyBehavior = EmptyBehavior.CloseParent
                )
                val destination = controller.getDestination(navigationHandle.key.instruction)

                if(destination.composableDestination is BottomSheetDestination) {
                    val state = rememberModalBottomSheetState(
                        ModalBottomSheetValue.HalfExpanded,
                        confirmStateChange = {
                            if(it == ModalBottomSheetValue.Hidden) {
                                getNavigationHandle().close()
                            }
                            true
                        }
                    )
                    ModalBottomSheetLayout(
                        sheetState = state,
                        scrimColor = Color.Transparent,
                        sheetContent = {
                            EnroContainer(controller = controller)
                        }
                    ){}
                }
                else {
                    EnroContainer(controller = controller)
                }

                DisposableEffect(key1 = true, effect = {
                    dialogConfiguration = when(destination.composableDestination) {
                        is DialogDestination -> destination.composableDestination.dialogConfiguration
                        is BottomSheetDestination -> destination.composableDestination.bottomSheetConfiguration
                        else -> TODO()
                    }

                    val anim = AnimationUtils.loadAnimation(requireContext(), dialogConfiguration.animations.asResource(requireActivity().theme).enter)
                    startAnimation(anim)
                    requireView().visibility = View.VISIBLE
                    requireView().animateToColor(dialogConfiguration.scrimColor)
                    onDispose {  }
                })
            }
        }

        return FrameLayout(requireContext()).apply {
            visibility = View.INVISIBLE
            addView(composeView)
        }
    }

    override fun dismiss() {
        view?.clearAnimation()
        val dismiss = { super.dismiss() }
        val anim = AnimationUtils.loadAnimation(requireContext(), dialogConfiguration.animations.asResource(requireActivity().theme).exit)
        anim.setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                dismiss()
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }
        })
        view?.findViewById<View>(composeViewId)?.startAnimation(anim)
        view?.animateToColor(Color.Transparent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog!!.apply {
            window!!.apply {
                setOnKeyListener { _, keyCode, event ->
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                        navigationContext.leafContext().getNavigationHandleViewModel().internalOnCloseRequested()
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

fun View.animateToColor(color: Color) {
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