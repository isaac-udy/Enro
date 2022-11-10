package dev.enro.core.fragment.container

import android.os.Bundle
import androidx.compose.material.ExperimentalMaterialApi
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import dagger.hilt.internal.GeneratedComponentManagerHolder
import dev.enro.core.*
import dev.enro.core.compose.ComposableNavigationBinding
import dev.enro.core.compose.dialog.BottomSheetDestination
import dev.enro.core.compose.dialog.DialogDestination
import dev.enro.core.container.asPresentInstruction
import dev.enro.core.fragment.FragmentNavigationBinding
import dev.enro.core.hosts.*

internal object FragmentFactory {

    private val generatedComponentManagerHolderClass = kotlin.runCatching {
        GeneratedComponentManagerHolder::class.java
    }.getOrNull()

    @OptIn(ExperimentalMaterialApi::class)
    fun createFragment(
        parentContext: NavigationContext<*>,
        binding: NavigationBinding<*, *>,
        instruction: AnyOpenInstruction
    ): Fragment {
        val isHiltContext = if (generatedComponentManagerHolderClass != null) {
            parentContext.contextReference is GeneratedComponentManagerHolder
        } else false

        val fragmentManager = when (parentContext.contextReference) {
            is FragmentActivity -> parentContext.contextReference.supportFragmentManager
            is Fragment -> parentContext.contextReference.childFragmentManager
            else -> throw IllegalStateException()
        }

        when (binding) {
            is FragmentNavigationBinding<*, *> -> {
                val isPresentation = instruction.navigationDirection is NavigationDirection.Present
                val isDialog =
                    DialogFragment::class.java.isAssignableFrom(binding.destinationType.java)

                val fragment = if (isPresentation && !isDialog) {
                    val wrappedKey = when {
                        isHiltContext -> OpenPresentableFragmentInHiltFragment(instruction.asPresentInstruction())
                        else -> OpenPresentableFragmentInFragment(instruction.asPresentInstruction())
                    }
                    createFragment(
                        parentContext = parentContext,
                        binding = parentContext.controller.bindingForKeyType(wrappedKey::class) as NavigationBinding<*, *>,
                        instruction = NavigationInstruction.Open.OpenInternal(
                            instructionId = instruction.instructionId,
                            navigationDirection = instruction.navigationDirection,
                            navigationKey = wrappedKey
                        )
                    )
                }
                else {
                    fragmentManager.fragmentFactory.instantiate(
                        binding.destinationType.java.classLoader!!,
                        binding.destinationType.java.name
                    ).apply {
                        arguments = Bundle().addOpenInstruction(instruction)
                    }
                }

                return fragment
            }
            is ComposableNavigationBinding<*, *> -> {

                val isDialog = DialogDestination::class.java.isAssignableFrom(binding.destinationType.java)
                            || BottomSheetDestination::class.java.isAssignableFrom(binding.destinationType.java)

                val wrappedKey = when {
                    isDialog -> when {
                        isHiltContext -> OpenComposableDialogInHiltFragment(instruction.asPresentInstruction())
                        else -> OpenComposableDialogInFragment(instruction.asPresentInstruction())
                    }
                    else -> when {
                        isHiltContext -> OpenComposableInHiltFragment(instruction, isRoot = false)
                        else -> OpenComposableInFragment(instruction, isRoot = false)
                    }
                }

                return createFragment(
                    parentContext = parentContext,
                    binding = parentContext.controller.bindingForKeyType(wrappedKey::class) as NavigationBinding<*, *>,
                    instruction = NavigationInstruction.Open.OpenInternal(
                        instructionId = instruction.instructionId,
                        navigationDirection = instruction.navigationDirection,
                        navigationKey = wrappedKey
                    )
                )
            }
            else -> throw IllegalStateException()
        }
    }
}