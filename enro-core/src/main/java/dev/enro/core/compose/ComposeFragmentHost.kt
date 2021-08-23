package dev.enro.core.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import dev.enro.core.*
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class HostedComposeKey(
    val instruction: NavigationInstruction.Open
) : NavigationKey

class ComposeFragmentHost : Fragment() {
    private val navigationHandle by navigationHandle<HostedComposeKey>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val state = remember {
                    EnroContainerState(
                        initialState = listOf(navigationHandle.key.instruction),
                        navigationHandle = getNavigationHandle()
                    )
                }
                EnroContainer(state = state)
                if (state.observeBackstackAsState().isEmpty()) {
                    navigationHandle.close()
                }
            }
        }
    }
}