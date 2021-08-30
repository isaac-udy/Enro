package dev.enro.core.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import dev.enro.core.EmptyBehavior
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.fragment.internal.fragmentHostFrom
import dev.enro.core.navigationHandle
import kotlinx.parcelize.Parcelize

internal abstract class AbstractComposeFragmentHostKey : NavigationKey {
    abstract val instruction: NavigationInstruction.Open
    abstract val fragmentContainerId: Int?
}

@Parcelize
internal data class ComposeFragmentHostKey(
    override val instruction: NavigationInstruction.Open,
    override val fragmentContainerId: Int?
) : AbstractComposeFragmentHostKey()

@Parcelize
internal data class HiltComposeFragmentHostKey(
    override val instruction: NavigationInstruction.Open,
    override val fragmentContainerId: Int?
) : AbstractComposeFragmentHostKey()

abstract class AbstractComposeFragmentHost : Fragment() {
    private val navigationHandle by navigationHandle<AbstractComposeFragmentHostKey>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentHost = container?.let { fragmentHostFrom(it) }

        return ComposeView(requireContext()).apply {
            setContent {
                val state = rememberEnroContainerController(
                    initialBackstack = listOf(navigationHandle.key.instruction),
                    accept = fragmentHost?.accept ?: { true },
                    emptyBehavior = EmptyBehavior.CloseParent
                )

                EnroContainer(controller = state)
            }
        }
    }
}

class ComposeFragmentHost : AbstractComposeFragmentHost()

@AndroidEntryPoint
class HiltComposeFragmentHost : AbstractComposeFragmentHost()