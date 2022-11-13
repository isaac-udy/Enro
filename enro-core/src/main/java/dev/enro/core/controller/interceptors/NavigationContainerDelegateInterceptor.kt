package dev.enro.core.controller.interceptors

import dev.enro.core.*
import dev.enro.core.interceptor.NavigationInstructionInterceptor

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