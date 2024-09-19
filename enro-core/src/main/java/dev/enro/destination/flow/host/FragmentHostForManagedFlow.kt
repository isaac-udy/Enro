package dev.enro.destination.flow.host

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.EnroInternalNavigationKey
import dev.enro.core.NavigationHost
import dev.enro.core.NavigationKey
import dev.enro.core.R
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.asPushInstruction
import dev.enro.core.fragment.container.navigationContainer
import dev.enro.core.navigationHandle
import kotlinx.parcelize.Parcelize

internal abstract class AbstractOpenManagedFlowInFragmentKey :
    NavigationKey.SupportsPush,
    NavigationKey.SupportsPresent,
    EnroInternalNavigationKey {

    abstract val instruction: AnyOpenInstruction
}

@Parcelize
internal data class OpenManagedFlowInFragment(
    override val instruction: AnyOpenInstruction,
) : AbstractOpenManagedFlowInFragmentKey()

@Parcelize
internal data class OpenManagedFlowInHiltFragment(
    override val instruction: AnyOpenInstruction,
) : AbstractOpenManagedFlowInFragmentKey()

public abstract class AbstractFragmentHostForManagedFlow : Fragment(), NavigationHost {

    private val navigation by navigationHandle<AbstractOpenManagedFlowInFragmentKey>()
    private val container by navigationContainer(
        containerId = R.id.enro_internal_single_fragment_frame_layout,
        rootInstruction = { navigation.key.instruction.asPushInstruction() },
        emptyBehavior = EmptyBehavior.CloseParent,
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FrameLayout(requireContext()).apply {
            id = R.id.enro_internal_single_fragment_frame_layout
        }
    }
}

internal class FragmentHostForManagedFlow : AbstractFragmentHostForManagedFlow()

@AndroidEntryPoint
internal class HiltFragmentHostForManagedFlow : AbstractFragmentHostForManagedFlow()
