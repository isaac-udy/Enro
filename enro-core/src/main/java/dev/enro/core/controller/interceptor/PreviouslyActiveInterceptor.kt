package dev.enro.core.controller.interceptor

import dev.enro.core.*

internal class PreviouslyActiveInterceptor : NavigationInstructionInterceptor{

    override fun intercept(
        instruction: NavigationInstruction.Open,
        parentContext: NavigationContext<*>,
        navigator: Navigator<out NavigationKey, out Any>
    ): NavigationInstruction.Open {
        return instruction
            .setPreviouslyActiveContainerId(parentContext)
    }

    private fun NavigationInstruction.Open.setPreviouslyActiveContainerId(
        parentContext: NavigationContext<*>
    ): NavigationInstruction.Open {
        return internal.copy(
            previouslyActiveId = parentContext.containerManager.activeContainer?.id
        )
    }
}