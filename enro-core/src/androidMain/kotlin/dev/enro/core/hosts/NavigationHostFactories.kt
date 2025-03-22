package dev.enro.core.hosts

import android.app.Activity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationHostFactory
import dev.enro.core.NavigationInstruction
import dev.enro.core.activity.ActivityNavigationBinding
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.compose.ComposableNavigationBinding
import dev.enro.core.container.asPresentInstruction
import dev.enro.core.fragment.FragmentNavigationBinding
import dev.enro.core.isHiltApplication
import dev.enro.core.isHiltContext
import dev.enro.destination.flow.ManagedFlowNavigationBinding
import dev.enro.destination.flow.host.OpenManagedFlowInFragment
import dev.enro.destination.flow.host.OpenManagedFlowInHiltFragment
import kotlin.reflect.full.isSubclassOf

internal class ActivityHost : NavigationHostFactory<Activity>(Activity::class) {
    override fun supports(
        navigationContext: NavigationContext<*>,
        instruction: NavigationInstruction.Open<*>,
    ): Boolean {
        return true
    }

    override fun wrap(
        navigationContext: NavigationContext<*>,
        instruction: NavigationInstruction.Open<*>,
    ): NavigationInstruction.Open<*> {
        val binding = requireNavigationBinding(instruction)
        if (binding is ActivityNavigationBinding) return instruction

        return instruction.internal.copy(
            navigationKey = when {
                navigationContext.isHiltApplication -> OpenInstructionInHiltActivity(instruction)
                else -> OpenInstructionInActivity(instruction)
            }
        )
    }
}

internal class DialogFragmentHost : NavigationHostFactory<DialogFragment>(DialogFragment::class) {

    override fun supports(
        navigationContext: NavigationContext<*>,
        instruction: NavigationInstruction.Open<*>,
    ): Boolean {
        val binding = requireNavigationBinding(instruction)
        val isSupportedBinding = binding is FragmentNavigationBinding ||
                binding is ComposableNavigationBinding ||
                binding is ManagedFlowNavigationBinding<*, *>

        return isSupportedBinding && instruction.navigationDirection == NavigationDirection.Present
    }

    override fun wrap(
        navigationContext: NavigationContext<*>,
        instruction: NavigationInstruction.Open<*>,
    ): NavigationInstruction.Open<*> {
        val isPresent = instruction.navigationDirection is NavigationDirection.Present
        if (!isPresent) cannotCreateHost(instruction)

        val binding = requireNavigationBinding(instruction)

        val isDialog = binding.destinationType.isSubclassOf(DialogFragment::class)
        if (isDialog) return instruction

        val key = when (binding) {
            is FragmentNavigationBinding -> when {
                navigationContext.isHiltContext -> OpenPresentableFragmentInHiltFragment(instruction.asPresentInstruction())
                else -> OpenPresentableFragmentInFragment(instruction.asPresentInstruction())
            }
            is ComposableNavigationBinding ->  when {
                navigationContext.isHiltContext -> OpenPresentableFragmentInHiltFragment(instruction.asPresentInstruction())
                else -> OpenPresentableFragmentInFragment(instruction.asPresentInstruction())
            }
            is ManagedFlowNavigationBinding<*, *> ->  when {
                navigationContext.isHiltContext -> OpenPresentableFragmentInHiltFragment(instruction.asPresentInstruction())
                else -> OpenPresentableFragmentInFragment(instruction.asPresentInstruction())
            }
            else -> cannotCreateHost(instruction)
        }
        return instruction.internal.copy(navigationKey = key)
    }
}

internal class FragmentHost : NavigationHostFactory<Fragment>(Fragment::class) {

    override fun supports(
        navigationContext: NavigationContext<*>,
        instruction: NavigationInstruction.Open<*>,
    ): Boolean {
        val binding = requireNavigationBinding(instruction)
        return binding is FragmentNavigationBinding ||
                binding is ComposableNavigationBinding ||
                binding is ManagedFlowNavigationBinding<*, *>
    }

    override fun wrap(
        navigationContext: NavigationContext<*>,
        instruction: NavigationInstruction.Open<*>,
    ): NavigationInstruction.Open<*> {
        val binding = requireNavigationBinding(instruction)

        return when (binding) {
            is FragmentNavigationBinding -> return instruction
            is ComposableNavigationBinding -> instruction.internal.copy(
                navigationKey = when {
                    navigationContext.isHiltContext -> OpenComposableInHiltFragment(instruction)
                    else -> OpenComposableInFragment(instruction)
                }
            )
            is ManagedFlowNavigationBinding<*, *> -> instruction.internal.copy(
                navigationKey = when {
                    navigationContext.isHiltContext -> OpenManagedFlowInHiltFragment(instruction)
                    else -> OpenManagedFlowInFragment(instruction)
                }
            )
            else -> cannotCreateHost(instruction)
        }
    }
}

internal class ComposableHost : NavigationHostFactory<ComposableDestination>(ComposableDestination::class) {
    override fun supports(
        navigationContext: NavigationContext<*>,
        instruction: NavigationInstruction.Open<*>,
    ): Boolean {
        val binding = requireNavigationBinding(instruction)
        return binding is ComposableNavigationBinding ||
                binding is ManagedFlowNavigationBinding<*, *>
    }

    override fun wrap(
        navigationContext: NavigationContext<*>,
        instruction: NavigationInstruction.Open<*>
    ): NavigationInstruction.Open<*> {
        if (!supports(navigationContext, instruction)) cannotCreateHost(instruction)
        return instruction
    }
}