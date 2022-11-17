package dev.enro.core.fragment.container

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import dev.enro.core.*
import dev.enro.core.controller.get

internal object FragmentFactory {
    fun createFragment(
        parentContext: NavigationContext<*>,
        instruction: AnyOpenInstruction
    ): Fragment {
        val fragmentManager = when (parentContext.contextReference) {
            is FragmentActivity -> parentContext.contextReference.supportFragmentManager
            is Fragment -> parentContext.contextReference.childFragmentManager
            else -> throw IllegalStateException()
        }

        val navigationHostFactory = parentContext.controller.dependencyScope.get<NavigationHostFactory>()

        val hostedInstruction = navigationHostFactory.createHostFor<Fragment>(instruction)
        val hostedBinding = parentContext.controller.bindingForKeyType(hostedInstruction.navigationKey::class) as NavigationBinding<*, *>

        return fragmentManager.fragmentFactory.instantiate(
            hostedBinding.destinationType.java.classLoader!!,
            hostedBinding.destinationType.java.name
        ).apply {
            arguments = Bundle().addOpenInstruction(hostedInstruction)
        }
    }

//    private val generatedComponentManagerHolderClass = kotlin.runCatching {
//        GeneratedComponentManagerHolder::class.java
//    }.getOrNull()
//
//    @OptIn(ExperimentalMaterialApi::class)
//    fun createFragment(
//        parentContext: NavigationContext<*>,
//        binding: NavigationBinding<*, *>,
//        instruction: AnyOpenInstruction
//    ): Fragment {
//        val isHiltContext = if (generatedComponentManagerHolderClass != null) {
//            parentContext.contextReference is GeneratedComponentManagerHolder
//        } else false
//
//        val fragmentManager = when (parentContext.contextReference) {
//            is FragmentActivity -> parentContext.contextReference.supportFragmentManager
//            is Fragment -> parentContext.contextReference.childFragmentManager
//            else -> throw IllegalStateException()
//        }
//
//        when (binding) {
//            is FragmentNavigationBinding<*, *> -> {
//                val isPresentation = instruction.navigationDirection is NavigationDirection.Present
//                val isDialog =
//                    DialogFragment::class.java.isAssignableFrom(binding.destinationType.java)
//
//                val fragment = if (isPresentation && !isDialog) {
//                    val wrappedKey = when {
//                        isHiltContext -> OpenPresentableFragmentInHiltFragment(instruction.asPresentInstruction())
//                        else -> OpenPresentableFragmentInFragment(instruction.asPresentInstruction())
//                    }
//                    createFragment(
//                        parentContext = parentContext,
//                        binding = parentContext.controller.bindingForKeyType(wrappedKey::class) as NavigationBinding<*, *>,
//                        instruction = NavigationInstruction.Open.OpenInternal(
//                            instructionId = instruction.instructionId,
//                            navigationDirection = instruction.navigationDirection,
//                            navigationKey = wrappedKey
//                        )
//                    )
//                }
//                else {
//                    fragmentManager.fragmentFactory.instantiate(
//                        binding.destinationType.java.classLoader!!,
//                        binding.destinationType.java.name
//                    ).apply {
//                        arguments = Bundle().addOpenInstruction(instruction)
//                    }
//                }
//
//                return fragment
//            }
//            is ComposableNavigationBinding<*, *> -> {
//
//                val isDialog =
//                    DialogDestination::class.java.isAssignableFrom(binding.destinationType.java)
//                            || BottomSheetDestination::class.java.isAssignableFrom(binding.destinationType.java)
//
//                val wrappedKey = when {
//                    isDialog -> when {
//                        isHiltContext -> OpenComposableDialogInHiltFragment(instruction.asPresentInstruction())
//                        else -> OpenComposableDialogInFragment(instruction.asPresentInstruction())
//                    }
//                    else -> when {
//                        isHiltContext -> OpenComposableInHiltFragment(instruction, isRoot = false)
//                        else -> OpenComposableInFragment(instruction, isRoot = false)
//                    }
//                }
//
//                return createFragment(
//                    parentContext = parentContext,
//                    binding = parentContext.controller.bindingForKeyType(wrappedKey::class) as NavigationBinding<*, *>,
//                    instruction = NavigationInstruction.Open.OpenInternal(
//                        instructionId = instruction.instructionId,
//                        navigationDirection = instruction.navigationDirection,
//                        navigationKey = wrappedKey
//                    )
//                )
//            }
//            else -> throw IllegalStateException()
//        }
//    }
}