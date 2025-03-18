package dev.enro.core.controller.interceptor

import dev.enro.core.*

internal object NavigationContainerDelegateInterceptor : NavigationInstructionInterceptor {

    override fun intercept(
        instruction: AnyOpenInstruction,
        context: NavigationContext<*>,
        binding: NavigationBinding<out NavigationKey, out Any>
    ): AnyOpenInstruction? {
        val parentContainer = context.parentContainer() ?: return instruction
        return parentContainer.interceptor.intercept(
            instruction,
            context,
            binding,
        )
    }

    override fun intercept(
        instruction: NavigationInstruction.Close,
        context: NavigationContext<*>
    ): NavigationInstruction? {
        val parentContainer = context.parentContainer() ?: return instruction
        return parentContainer.interceptor.intercept(
            instruction,
            context,
        )
    }
}