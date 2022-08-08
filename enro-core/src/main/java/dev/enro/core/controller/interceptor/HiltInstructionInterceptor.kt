package dev.enro.core.controller.interceptor

import dagger.hilt.internal.GeneratedComponentManager
import dagger.hilt.internal.GeneratedComponentManagerHolder
import dev.enro.core.*
import dev.enro.core.hosts.*
import dev.enro.core.hosts.OpenComposableDialogInFragment
import dev.enro.core.hosts.OpenComposableDialogInHiltFragment
import dev.enro.core.hosts.OpenComposableInFragment
import dev.enro.core.hosts.OpenComposableInHiltFragment
import dev.enro.core.hosts.OpenInstructionInActivity
import dev.enro.core.hosts.OpenInstructionInHiltActivity

class HiltInstructionInterceptor : NavigationInstructionInterceptor {

    val generatedComponentManagerClass = kotlin.runCatching {
        GeneratedComponentManager::class.java
    }.getOrNull()

    val generatedComponentManagerHolderClass = kotlin.runCatching {
        GeneratedComponentManagerHolder::class.java
    }.getOrNull()

    override fun intercept(
        instruction: AnyOpenInstruction,
        parentContext: NavigationContext<*>,
        navigator: Navigator<out NavigationKey, out Any>
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