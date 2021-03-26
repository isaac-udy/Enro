package dev.enro.core.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.controller.navigationController
import dev.enro.core.navigationHandle
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class HostedComposeKey(
    val instruction: NavigationInstruction.Open
) : NavigationKey

class ComposeFragmentHost : Fragment() {
    private val navigationHandle by navigationHandle<HostedComposeKey>()

    internal val rootContainer = ComposableContainer { requireActivity().application.navigationController }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if(savedInstanceState == null) {
            val controller = navigationHandle.controller
            val composeKey = navigationHandle.key.instruction.navigationKey
            val destination = controller.navigatorForKeyType(composeKey::class)!!.contextType.java
                .newInstance() as ComposableDestination

            destination.instruction = navigationHandle.key.instruction
            rootContainer.push(destination)
        }

        return ComposeView(requireContext()).apply {
            setContent {
                rootContainer.Render()
            }
        }
    }
}