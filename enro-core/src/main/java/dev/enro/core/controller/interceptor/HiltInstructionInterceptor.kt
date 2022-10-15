package dev.enro.core.controller.interceptor

import dagger.hilt.internal.GeneratedComponentManager
import dagger.hilt.internal.GeneratedComponentManagerHolder
import dev.enro.core.*
import dev.enro.core.hosts.*

object HiltInstructionInterceptor : NavigationInstructionInterceptor {

    private val generatedComponentManagerClass = kotlin.runCatching {
        GeneratedComponentManager::class.java
    }.getOrNull()

    private val generatedComponentManagerHolderClass = kotlin.runCatching {
        GeneratedComponentManagerHolder::class.java
    }.getOrNull()

    override fun intercept(
        instruction: AnyOpenInstruction,
        parentContext: NavigationContext<*>,
        binding: NavigationBinding<out NavigationKey, out Any>
    ): AnyOpenInstruction {

        val isHiltApplication = if(generatedComponentManagerClass != null) {
            parentContext.activity.application is GeneratedComponentManager<*>
        } else false

        val isHiltActivity = if(generatedComponentManagerHolderClass != null) {
            parentContext.activity is GeneratedComponentManagerHolder
        } else false

        val navigationKey = instruction.navigationKey

        if(navigationKey is OpenInstructionInActivity && isHiltApplication) {
            return instruction.internal.copy(
                navigationKey = OpenInstructionInHiltActivity(
                    instruction = navigationKey.instruction
                )
            )
        }

        if(navigationKey is OpenComposableInFragment && isHiltActivity) {
            return instruction.internal.copy(
                navigationKey = OpenComposableInHiltFragment(
                    instruction = navigationKey.instruction,
                    isRoot = navigationKey.isRoot
                )
            )
        }

        if(navigationKey is OpenComposableDialogInFragment && isHiltActivity) {
            return instruction.internal.copy(
                navigationKey = OpenComposableDialogInHiltFragment(
                    instruction = navigationKey.instruction,
                )
            )
        }

        if(navigationKey is OpenPresentableFragmentInFragment && isHiltActivity) {
            return instruction.internal.copy(
                navigationKey = OpenPresentableFragmentInHiltFragment(
                    instruction = navigationKey.instruction,
                )
            )
        }

        return instruction
    }
}