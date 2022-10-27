package dev.enro.core.container

import dev.enro.core.*
import dev.enro.core.controller.interceptor.NavigationInstructionInterceptor

internal class NavigationContainerInterceptor : NavigationInstructionInterceptor {
    override fun intercept(
        instruction: AnyOpenInstruction,
        context: NavigationContext<*>,
        binding: NavigationBinding<out NavigationKey, out Any>
    ): AnyOpenInstruction? {
        return instruction
    }

    override fun intercept(
        instruction: NavigationInstruction.Close,
        context: NavigationContext<*>
    ): NavigationInstruction? {
        return instruction
    }
}