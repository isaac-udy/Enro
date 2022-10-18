package dev.enro.core.controller.interceptor

import dev.enro.core.*

public interface NavigationInstructionInterceptor {
    public fun intercept(
        instruction: AnyOpenInstruction,
        parentContext: NavigationContext<*>,
        binding: NavigationBinding<out NavigationKey, out Any>
    ): AnyOpenInstruction? {
        return instruction
    }

    public fun intercept(
        instruction: NavigationInstruction.Close,
        context: NavigationContext<*>
    ): NavigationInstruction? {
        return instruction
    }
}