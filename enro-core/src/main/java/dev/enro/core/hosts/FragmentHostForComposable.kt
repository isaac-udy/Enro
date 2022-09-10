package dev.enro.core.hosts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import dev.enro.core.*
import dev.enro.core.compose.EnroContainer
import dev.enro.core.compose.rememberEnroContainerController
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.asPushInstruction
import kotlinx.parcelize.Parcelize

internal abstract class AbstractOpenComposableInFragmentKey :
    NavigationKey.SupportsPush,
    NavigationKey.SupportsPresent,
    EnroInternalNavigationKey {

    abstract val instruction: AnyOpenInstruction
    abstract val isRoot: Boolean
}

@Parcelize
internal data class OpenComposableInFragment(
    override val instruction: AnyOpenInstruction,
    override val isRoot: Boolean
) : AbstractOpenComposableInFragmentKey()

@Parcelize
internal data class OpenComposableInHiltFragment(
    override val instruction: AnyOpenInstruction,
    override val isRoot: Boolean
) : AbstractOpenComposableInFragmentKey()

abstract class AbstractFragmentHostForComposable : Fragment() {
    private val navigationHandle by navigationHandle<AbstractOpenComposableInFragmentKey>()

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

class FragmentHostForComposable : AbstractFragmentHostForComposable()

@AndroidEntryPoint
class HiltFragmentHostForComposable : AbstractFragmentHostForComposable()