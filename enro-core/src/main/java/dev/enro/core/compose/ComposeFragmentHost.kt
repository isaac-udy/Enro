package dev.enro.core.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.close
import dev.enro.core.fragment.internal.fragmentHostFrom
import dev.enro.core.navigationHandle
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ComposeFragmentHostKey(
    val instruction: NavigationInstruction.Open,
    val fragmentContainerId: Int?
) : NavigationKey

abstract class AbstractComposeFragmentHost : Fragment() {
    private val navigationHandle by navigationHandle<ComposeFragmentHostKey>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentHost = container?.let { fragmentHostFrom(it) }

        return ComposeView(requireContext()).apply {
            setContent {
                val state = rememberEnroContainerState(
                    initialState = listOf(navigationHandle.key.instruction),
                    accept = fragmentHost?.accept ?: { true }
                )

                EnroContainer(state = state)
                if (state.observeBackstackState().backstack.isEmpty()) {
                    navigationHandle.close()
                }
            }
        }
    }
}

class ComposeFragmentHost : AbstractComposeFragmentHost()

@AndroidEntryPoint
class HiltComposeFragmentHost : AbstractComposeFragmentHost()