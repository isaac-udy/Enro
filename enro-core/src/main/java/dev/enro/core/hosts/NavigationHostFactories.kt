package dev.enro.core.hosts

import android.app.Activity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import dev.enro.core.*
import dev.enro.destination.activity.ActivityNavigationBinding
import dev.enro.destination.compose.ComposableNavigationBinding
import dev.enro.core.container.asPresentInstruction
import dev.enro.destination.fragment.FragmentNavigationBinding
import dev.enro.android.hilt.isHiltApplication
import dev.enro.android.hilt.isHiltContext

internal class ActivityHost : NavigationHostFactory<Activity>(Activity::class.java) {
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

@OptIn(ExperimentalMaterialApi::class)
internal class DialogFragmentHost : NavigationHostFactory<DialogFragment>(DialogFragment::class.java) {

    override fun supports(
        navigationContext: NavigationContext<*>,
        instruction: NavigationInstruction.Open<*>,
    ): Boolean {
        val binding = requireNavigationBinding(instruction)
        val isSupportedBinding = binding is FragmentNavigationBinding || binding is ComposableNavigationBinding
        return isSupportedBinding && instruction.navigationDirection == NavigationDirection.Present
    }

    override fun wrap(
        navigationContext: NavigationContext<*>,
        instruction: NavigationInstruction.Open<*>,
    ): NavigationInstruction.Open<*> {
        val isPresent = instruction.navigationDirection is NavigationDirection.Present
        if (!isPresent) cannotCreateHost(instruction)

        val binding = requireNavigationBinding(instruction)

        val isDialog = DialogFragment::class.java.isAssignableFrom(binding.destinationType.java)
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
            else -> cannotCreateHost(instruction)
        }
        return instruction.internal.copy(navigationKey = key)
    }
}

internal class FragmentHost : NavigationHostFactory<Fragment>(Fragment::class.java) {

    override fun supports(
        navigationContext: NavigationContext<*>,
        instruction: NavigationInstruction.Open<*>,
    ): Boolean {
        val binding = requireNavigationBinding(instruction)
        return binding is FragmentNavigationBinding || binding is ComposableNavigationBinding
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
            else -> cannotCreateHost(instruction)
        }
    }
}