package dev.enro.core.hosts

import android.app.Activity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationHostFactory
import dev.enro.core.NavigationInstruction
import dev.enro.core.cannotCreateHost
import dev.enro.core.compose.ComposableNavigationBinding
import dev.enro.core.compose.dialog.BottomSheetDestination
import dev.enro.core.compose.dialog.DialogDestination
import dev.enro.core.container.asPresentInstruction
import dev.enro.core.controller.repository.NavigationBindingRepository
import dev.enro.core.controller.repository.requireBindingForInstruction
import dev.enro.core.fragment.FragmentNavigationBinding

internal class ActivityHost : NavigationHostFactory<Activity> {
    override val hostType: Class<Activity> = Activity::class.java

    override fun supports(instruction: NavigationInstruction.Open<*>): Boolean {
        return true
    }

    override fun wrap(instruction: NavigationInstruction.Open<*>): NavigationInstruction.Open<*> {
        return instruction.internal.copy(
            navigationKey = OpenInstructionInActivity(instruction),
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
internal class DialogFragmentHost(
    private val navigationBindingRepository: NavigationBindingRepository,
) : NavigationHostFactory<DialogFragment> {
    override val hostType: Class<DialogFragment> = DialogFragment::class.java

    override fun supports(instruction: NavigationInstruction.Open<*>): Boolean {
        val binding = navigationBindingRepository.requireBindingForInstruction(instruction)
        val isSupportedBinding = binding is FragmentNavigationBinding || binding is ComposableNavigationBinding
        return isSupportedBinding && instruction.navigationDirection == NavigationDirection.Present
    }

    override fun wrap(instruction: NavigationInstruction.Open<*>): NavigationInstruction.Open<*> {
        val isPresent = instruction.navigationDirection is NavigationDirection.Present
        if (!isPresent) cannotCreateHost(instruction)

        val binding = navigationBindingRepository.requireBindingForInstruction(instruction)

        val isDialog = DialogFragment::class.java.isAssignableFrom(binding.destinationType.java)
        if (isDialog) return instruction

        val key = when (binding) {
            is FragmentNavigationBinding -> OpenPresentableFragmentInFragment(instruction.asPresentInstruction())
            is ComposableNavigationBinding -> {
                val isComposableDialog =
                    DialogDestination::class.java.isAssignableFrom(binding.destinationType.java)
                            || BottomSheetDestination::class.java.isAssignableFrom(binding.destinationType.java)
                when {
                    isComposableDialog -> OpenComposableDialogInFragment(instruction.asPresentInstruction())
                    else -> OpenPresentableFragmentInFragment(instruction.asPresentInstruction())
                }
            }
            else -> cannotCreateHost(instruction)
        }
        return instruction.internal.copy(navigationKey = key)
    }
}

internal class FragmentHost(
    private val navigationBindingRepository: NavigationBindingRepository,
) : NavigationHostFactory<Fragment> {
    override val hostType: Class<Fragment> = Fragment::class.java

    override fun supports(instruction: NavigationInstruction.Open<*>): Boolean {
        val binding = navigationBindingRepository.requireBindingForInstruction(instruction)
        return binding is FragmentNavigationBinding || binding is ComposableNavigationBinding
    }

    override fun wrap(instruction: NavigationInstruction.Open<*>): NavigationInstruction.Open<*> {
        val binding = navigationBindingRepository.requireBindingForInstruction(instruction)

        return when (binding) {
            is FragmentNavigationBinding -> return instruction
            is ComposableNavigationBinding -> instruction.internal.copy(
                navigationKey = OpenComposableInFragment(instruction)
            )
            else -> cannotCreateHost(instruction)
        }
    }
}