package dev.enro.core.controller.interceptor

import dagger.hilt.internal.GeneratedComponentManager
import dagger.hilt.internal.GeneratedComponentManagerHolder
import dev.enro.core.*
import dev.enro.core.compose.ComposeFragmentHostKey
import dev.enro.core.compose.HiltComposeFragmentHostKey
import dev.enro.core.fragment.internal.HiltSingleFragmentKey
import dev.enro.core.fragment.internal.SingleFragmentKey

class HiltInstructionInterceptor : NavigationInstructionInterceptor {

    val generatedComponentManagerClass = kotlin.runCatching {
        GeneratedComponentManager::class.java
    }.getOrNull()

    val generatedComponentManagerHolderClass = kotlin.runCatching {
        GeneratedComponentManagerHolder::class.java
    }.getOrNull()

    override fun intercept(
        instruction: NavigationInstruction.Open,
        parentContext: NavigationContext<*>,
        navigator: Navigator<out NavigationKey, out Any>
    ): NavigationInstruction.Open {

        val isHiltApplication = if(generatedComponentManagerClass != null) {
            parentContext.activity.application is GeneratedComponentManager<*>
        } else false

        val isHiltActivity = if(generatedComponentManagerHolderClass != null) {
            parentContext.activity is GeneratedComponentManagerHolder
        } else false

        val navigationKey = instruction.navigationKey

        if(navigationKey is SingleFragmentKey && isHiltApplication) {
            return instruction.internal.copy(
                navigationKey = HiltSingleFragmentKey(
                    instruction = navigationKey.instruction
                )
            )
        }

        if(navigationKey is ComposeFragmentHostKey && isHiltActivity) {
            return instruction.internal.copy(
                navigationKey = HiltComposeFragmentHostKey(
                    instruction = navigationKey.instruction,
                    fragmentContainerId = navigationKey.fragmentContainerId
                )
            )
        }

        return instruction
    }
}