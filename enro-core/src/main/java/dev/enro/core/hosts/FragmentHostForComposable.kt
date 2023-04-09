package dev.enro.core.hosts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import dev.enro.core.*
import dev.enro.core.compose.rememberNavigationContainer
import dev.enro.core.container.EmptyBehavior
import dev.enro.core.container.backstackOf
import kotlinx.parcelize.Parcelize

internal abstract class AbstractOpenComposableInFragmentKey :
    NavigationKey.SupportsPush,
    NavigationKey.SupportsPresent,
    EnroInternalNavigationKey {

    abstract val instruction: AnyOpenInstruction
}

@Parcelize
internal data class OpenComposableInFragment(
    override val instruction: AnyOpenInstruction,
) : AbstractOpenComposableInFragmentKey()

@Parcelize
internal data class OpenComposableInHiltFragment(
    override val instruction: AnyOpenInstruction,
) : AbstractOpenComposableInFragmentKey()

public abstract class AbstractFragmentHostForComposable : Fragment(), NavigationHost {
    private val navigationHandle by navigationHandle<AbstractOpenComposableInFragmentKey>()

    private val isRoot by lazy {
        val activity = requireActivity()
        if (activity !is AbstractActivityHostForAnyInstruction) return@lazy false
        val hasParent = parentFragment != null
        if (hasParent) return@lazy false
        val activityKey = activity.getNavigationHandle().instruction.navigationKey as AbstractOpenInstructionInActivityKey
        return@lazy activityKey.instruction.instructionId == navigationHandle.key.instruction.instructionId
    }

    override fun accept(instruction: NavigationInstruction.Open<*>): Boolean {
        return isRoot
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val isRoot = isRoot
        val initialBackstack = navigationHandle.key.instruction
        return ComposeView(requireContext()).apply {
            id = R.id.enro_internal_compose_fragment_view_id
            setContent {
                val navigation = dev.enro.core.compose.navigationHandle()
                rememberNavigationContainer(
                    key = NavigationContainerKey.FromName("FragmentHostForCompose"),
                    initialBackstack = backstackOf(initialBackstack),
                    accept = { isRoot },
                    emptyBehavior = when {
                        isRoot -> EmptyBehavior.CloseParent
                        else -> EmptyBehavior.Action {
                            navigation.close()
                            false
                        }
                    },
                ).Render()
            }
        }
    }
}

internal class FragmentHostForComposable : AbstractFragmentHostForComposable()

@AndroidEntryPoint
internal class HiltFragmentHostForComposable : AbstractFragmentHostForComposable()