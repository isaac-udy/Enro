package dev.enro.core.controller.interceptor

import dev.enro.core.*

internal class PreviouslyActiveInterceptor : NavigationInstructionInterceptor{

    override fun intercept(
        instruction: AnyOpenInstruction,
        parentContext: NavigationContext<*>,
        navigator: Navigator<out NavigationKey, out Any>
    ): AnyOpenInstruction {
        return instruction
            .setPreviouslyActiveContainerId(parentContext) as AnyOpenInstruction
    }

    private fun AnyOpenInstruction.setPreviouslyActiveContainerId(
        parentContext: NavigationContext<*>
    ): AnyOpenInstruction {
        return internal.copy(
            previouslyActiveId = parentContext.containerManager.activeContainer?.id
        )
    }
}