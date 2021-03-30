package dev.enro.core.compose

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.controller.navigationController
import dev.enro.core.navigationContext
import dev.enro.core.navigationHandle
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class HostedComposeKey(
    val instruction: NavigationInstruction.Open
) : NavigationKey

class ComposeFragmentHost : Fragment() {
    private val navigationHandle by navigationHandle<HostedComposeKey>()

    internal lateinit var rootContainer: ComposableContainer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootContainer = ComposableContainer(
            initialState=  savedInstanceState?.getParcelableArrayList("backstackState") ?: listOf(navigationHandle.key.instruction),
            navigationController = { requireActivity().application.navigationController },
            hostContext = { navigationContext },
        )

        Log.e("CREATED CFH", "${this.hashCode()}, ${rootContainer.hashCode()}, ${rootContainer.backstackState.value}")
        return ComposeView(requireContext()).apply {
            setContent {
                rootContainer.Render()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList("backstackState", ArrayList(rootContainer.backstackState.value.orEmpty()))
    }
}