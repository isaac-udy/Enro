package dev.enro.core.hosts

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import dagger.hilt.android.AndroidEntryPoint
import dev.enro.core.*
import dev.enro.core.compose.dialog.*
import dev.enro.core.compose.dialog.EnroBottomSheetContainer
import dev.enro.core.compose.dialog.EnroDialogContainer
import dev.enro.core.compose.rememberEnroContainerController
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.asPushInstruction
import dev.enro.extensions.animate
import dev.enro.extensions.createFullscreenDialog
import kotlinx.parcelize.Parcelize


internal abstract class AbstractComposeDialogFragmentHostKey : NavigationKey {
    abstract val instruction: OpenPresentInstruction
}

@Parcelize
internal data class ComposeDialogFragmentHostKey(
    override val instruction: OpenPresentInstruction
) : AbstractComposeDialogFragmentHostKey()

@Parcelize
internal data class HiltComposeDialogFragmentHostKey(
    override val instruction: OpenPresentInstruction
) : AbstractComposeDialogFragmentHostKey()


abstract class AbstractComposeDialogFragmentHost : DialogFragment() {
    private val navigationHandle by navigationHandle<AbstractComposeDialogFragmentHostKey>()

    private lateinit var dialogConfiguration: DialogConfiguration

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = createFullscreenDialog()

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View  = ComposeView(requireContext()).apply {
        id = R.id.enro_internal_compose_dialog_fragment_view_id
        isVisible = false

        setContent {
            val instruction = navigationHandle.key.instruction.asPushInstruction()
            val controller = rememberEnroContainerController(
                initialBackstack = listOf(instruction),
                accept = { false },
                emptyBehavior = EmptyBehavior.CloseParent
            )

            val destination = controller.requireDestinationContext(instruction).destination
            dialogConfiguration = when (destination) {
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

    private fun enter() {
        val activity = activity ?: return
        val view = view ?: return

        view.isVisible = true
        view.clearAnimation()
        view.animate(
            dialogConfiguration.animations.asResource(activity.theme).enter,
        )
    }

    override fun dismiss() {
        val view = view ?: run {
            super.dismiss()
            return
        }
        if(dialogConfiguration.isDismissed.value) return
        dialogConfiguration.isDismissed.value = true

        view.isVisible = true
        view.clearAnimation()
        view.animate(
            dialogConfiguration.animations.asResource(requireActivity().theme).exit,
            onAnimationEnd = {
                super.dismiss()
            }
        )
    }
}

class ComposeDialogFragmentHost : AbstractComposeDialogFragmentHost()

@AndroidEntryPoint
class HiltComposeDialogFragmentHost : AbstractComposeDialogFragmentHost()
