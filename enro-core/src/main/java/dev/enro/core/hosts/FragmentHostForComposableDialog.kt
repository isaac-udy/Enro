package dev.enro.core.hosts

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import dagger.hilt.android.AndroidEntryPoint
import dev.enro.core.*
import dev.enro.core.compose.rememberEnroContainerController
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.asPushInstruction
import dev.enro.extensions.animate
import dev.enro.extensions.createFullscreenDialog
import kotlinx.parcelize.Parcelize


internal abstract class AbstractOpenComposableDialogInFragmentKey :
    NavigationKey,
    EnroInternalNavigationKey {

    abstract val instruction: OpenPresentInstruction
}

@Parcelize
internal data class OpenComposableDialogInFragment(
    override val instruction: OpenPresentInstruction
) : AbstractOpenComposableDialogInFragmentKey()

@Parcelize
internal data class OpenComposableDialogInHiltFragment(
    override val instruction: OpenPresentInstruction
) : AbstractOpenComposableDialogInFragmentKey()


public abstract class AbstractFragmentHostForComposableDialog : DialogFragment() {
    private val navigationHandle by navigationHandle<AbstractOpenComposableDialogInFragmentKey>()

//    private lateinit var dialogConfiguration: DialogConfiguration

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = createFullscreenDialog()

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = object : AbstractComposeView(requireContext()), DialogWindowProvider {

        override val window: Window = requireDialog().window!!

        init {
            id = R.id.enro_internal_compose_dialog_fragment_view_id
            isVisible = false
        }

        @Composable
        override fun Content() {
            val instruction = navigationHandle.key.instruction.asPushInstruction()
            val container = rememberEnroContainerController(
                initialBackstack = listOf(instruction),
                accept = { false },
                emptyBehavior = EmptyBehavior.CloseParent
            )
            container.Render()

//            val destination = container.requireDestinationOwner(instruction).destination
//            dialogConfiguration = when (destination) {
//                is BottomSheetDestination -> {
//                    container.Render()
//                    destination.bottomSheetConfiguration
//                }
//                is DialogDestination -> {
//                    container.Render()
//                    destination.dialogConfiguration
//                }
//                else -> throw EnroException.DestinationIsNotDialogDestination("The @Composable destination for ${navigationHandle.key::class.java.simpleName} must be a DialogDestination or a BottomSheetDestination")
//            }

            DisposableEffect(true) {
                enter()
                onDispose { }
            }
        }
    }

    private fun enter() {
        val activity = activity ?: return
        val view = view ?: return

        view.isVisible = true
        view.clearAnimation()
        view.animate(
            DefaultAnimations.none.asResource(activity.theme).enter,//dialogConfiguration.animations.asResource(activity.theme).enter,
        )
    }

    override fun dismiss() {
        val view = view ?: run {
            super.dismiss()
            return
        }
//        if (dialogConfiguration.isDismissed.value) return
//        dialogConfiguration.isDismissed.value = true

        view.isVisible = true
        view.clearAnimation()
        view.animate(
            DefaultAnimations.none.asResource(requireActivity().theme).exit,
            onAnimationEnd = {
                super.dismiss()
            }
        )
    }
}

internal class FragmentHostForComposableDialog : AbstractFragmentHostForComposableDialog()

@AndroidEntryPoint
internal class HiltFragmentHostForComposableDialog : AbstractFragmentHostForComposableDialog()
