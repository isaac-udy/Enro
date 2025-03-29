package dev.enro.destination.flow.host

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import dev.enro.core.AnyOpenInstruction
import dev.enro.core.EnroInternalNavigationKey
import dev.enro.core.NavigationContainerKey
import dev.enro.core.NavigationHost
import dev.enro.core.NavigationKey
import dev.enro.core.R
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.acceptNone
import dev.enro.core.container.backstackOf
import dev.enro.core.navigationHandle
import kotlinx.serialization.Serializable

internal abstract class AbstractOpenManagedFlowInFragmentKey :
    NavigationKey.SupportsPush,
    NavigationKey.SupportsPresent,
    EnroInternalNavigationKey {

    abstract val instruction: AnyOpenInstruction
}

@Serializable
internal data class OpenManagedFlowInFragment(
    override val instruction: AnyOpenInstruction,
) : AbstractOpenManagedFlowInFragmentKey()

@Serializable
internal data class OpenManagedFlowInHiltFragment(
    override val instruction: AnyOpenInstruction,
) : AbstractOpenManagedFlowInFragmentKey()

internal abstract class AbstractFragmentHostForManagedFlow : Fragment(), NavigationHost {

    private val navigation by navigationHandle<AbstractOpenManagedFlowInFragmentKey>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val initialBackstack = navigation.key.instruction
        return ComposeView(requireContext()).apply {
            id = R.id.enro_internal_compose_fragment_view_id
            setContent {
                val composableContainer = rememberNavigationContainer(
                    key = NavigationContainerKey.FromName("FragmentHostForManagedFlow"),
                    initialBackstack = backstackOf(initialBackstack),
                    filter = acceptNone(),
                    emptyBehavior = EmptyBehavior.CloseParent,
                )
                Box(modifier = Modifier.fillMaxSize()) {
                    composableContainer.Render()
                }
                SideEffect {
                    composableContainer.setActive()
                }
            }
        }
    }
}

internal class FragmentHostForManagedFlow : AbstractFragmentHostForManagedFlow()

@AndroidEntryPoint
internal class HiltFragmentHostForManagedFlow : AbstractFragmentHostForManagedFlow()
