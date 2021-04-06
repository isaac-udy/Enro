package dev.enro.core.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import dev.enro.core.*
import dev.enro.core.controller.navigationController
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class HostedComposeKey(
    val instruction: NavigationInstruction.Open
) : NavigationKey

class ComposeFragmentHost : Fragment() {
    private val navigationHandle by navigationHandle<HostedComposeKey>()

    internal lateinit var rootContainer: ComposableContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootContainer = ComposableContainer(
            initialState = savedInstanceState?.getParcelableArrayList("backstackState")
                ?: listOf(),
            navigationController = { requireActivity().application.navigationController },
            hostContext = { navigationContext },
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                rootContainer.Render()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if(savedInstanceState == null && rootContainer.backstackState.value.orEmpty().isEmpty()) {
            navigationHandle.executeInstruction(navigationHandle.key.instruction)
        }
        if(rootContainer.backstackState.value.orEmpty().isEmpty()) {
            navigationHandle.close()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(
            "backstackState",
            ArrayList(rootContainer.backstackState.value.orEmpty())
        )
    }
}