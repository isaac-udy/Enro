package dev.enro.core.hosts

import android.app.Activity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import dagger.hilt.internal.GeneratedComponentManagerHolder
import dev.enro.core.*
import dev.enro.core.compose.ComposableDestination
import dev.enro.core.compose.dialog.BottomSheetDestination
import dev.enro.core.compose.dialog.DialogDestination
import dev.enro.core.container.asDirection
import dev.enro.core.container.asPresentInstruction
import dev.enro.core.controller.repository.NavigationBindingRepository

internal class NavigationHostFactoryImpl(
    private val bindingRepository: NavigationBindingRepository
) : NavigationHostFactory {

    private val generatedComponentManagerHolderClass = kotlin.runCatching {
        GeneratedComponentManagerHolder::class.java
    }.getOrNull()

    override fun canCreateHostFor(
        targetContextType: Class<*>,
        binding: NavigationBinding<*, *>
    ): Boolean {
        if (targetContextType == binding.baseType.java) return true

        return when (targetContextType) {
            Activity::class.java -> true
            Fragment::class.java -> when (binding.baseType) {
                ComposableDestination::class -> true
                else -> false
            }
            else -> false
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    override fun createHostFor(
        targetContextType: Class<*>,
        instruction: NavigationInstruction.Open<*>
    ): NavigationInstruction.Open<*> {
        val binding = bindingRepository.bindingForKeyType(instruction.navigationKey::class)
            ?: throw EnroException.MissingNavigationBinding(instruction.navigationKey)
        val bindingType = binding.baseType

        val navigationKey = when (targetContextType) {
            Activity::class.java -> when (bindingType) {
                Activity::class.java -> instruction.navigationKey
                else -> OpenInstructionInActivity(instruction)
            }
            Fragment::class.java -> when (bindingType) {
                Fragment::class -> {
                    val isPresentation = instruction.navigationDirection is NavigationDirection.Present
                    val isDialog =
                        DialogFragment::class.java.isAssignableFrom(binding.destinationType.java)

                    if (isPresentation && !isDialog) OpenPresentableFragmentInFragment(instruction.asPresentInstruction())
                    else instruction.navigationKey
                }
                ComposableDestination::class -> {
                    val isPresentation = instruction.navigationDirection is NavigationDirection.Present

                    val isDialog =
                        DialogDestination::class.java.isAssignableFrom(binding.destinationType.java)
                                || BottomSheetDestination::class.java.isAssignableFrom(binding.destinationType.java)
                    when {
                        isDialog -> OpenComposableDialogInFragment(instruction.asPresentInstruction())
                        isPresentation -> OpenPresentableFragmentInFragment(instruction.asPresentInstruction())
                        else -> OpenComposableInFragment(instruction)
                    }
                }
                else -> throw EnroException.CannotCreateHostForType(
                    targetContextType,
                    bindingType.java
                )
            }
            ComposableDestination::class.java -> throw EnroException.CannotCreateHostForType(
                targetContextType,
                bindingType.java
            )
            else -> throw EnroException.CannotCreateHostForType(targetContextType, bindingType.java)
        }

        return NavigationInstruction.DefaultDirection(navigationKey)
            .asDirection(instruction.navigationDirection)
            .internal
            .copy(
                instructionId = instruction.instructionId,
                resultId = instruction.internal.resultId
            )
    }
}
