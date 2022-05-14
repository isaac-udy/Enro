package dev.enro.core.controller.interceptor

import dev.enro.core.*

interface NavigationInstructionInterceptor {
    fun intercept(
        instruction: AnyOpenInstruction,
        parentContext: NavigationContext<*>,
        navigator: Navigator<out NavigationKey, out Any>
    ): AnyOpenInstruction? { return instruction }

    fun intercept(
        instruction: NavigationInstruction.Close,
        context: NavigationContext<*>
    ): NavigationInstruction? { return instruction }
}