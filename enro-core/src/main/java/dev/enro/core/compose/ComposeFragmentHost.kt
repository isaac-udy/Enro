package dev.enro.core.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import dev.enro.core.*
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.asPushInstruction
import kotlinx.parcelize.Parcelize

internal abstract class AbstractComposeFragmentHostKey : NavigationKey.SupportsPush, NavigationKey.SupportsPresent {
    abstract val instruction: AnyOpenInstruction
    abstract val isRoot: Boolean
}

@Parcelize
internal data class ComposeFragmentHostKey(
    override val instruction: AnyOpenInstruction,
    override val isRoot: Boolean
) : AbstractComposeFragmentHostKey()

@Parcelize
internal data class HiltComposeFragmentHostKey(
    override val instruction: AnyOpenInstruction,
    override val isRoot: Boolean
) : AbstractComposeFragmentHostKey()

abstract class AbstractComposeFragmentHost : Fragment() {
    private val navigationHandle by navigationHandle<AbstractComposeFragmentHostKey>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val state = rememberEnroContainerController(
                    initialBackstack = listOf(navigationHandle.key.instruction.asPushInstruction()),
                    accept = { navigationHandle.key.isRoot },
                    emptyBehavior = EmptyBehavior.CloseParent
                )

                EnroContainer(container = state)
            }
        }
    }
}

class ComposeFragmentHost : AbstractComposeFragmentHost()

@AndroidEntryPoint
class HiltComposeFragmentHost : AbstractComposeFragmentHost()