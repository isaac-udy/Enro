package dev.enro.core.controller.interceptors

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationKey
import dev.enro.core.interceptor.NavigationInstructionInterceptor
import dev.enro.core.usecase.ensureContextIsSetFrom

internal object NavigationInstructionContextInterceptor : NavigationInstructionInterceptor {
    override fun intercept(
        instruction: AnyOpenInstruction,
        context: NavigationContext<*>,
        binding: NavigationBinding<out NavigationKey, out Any>
    ): AnyOpenInstruction {
        return instruction.ensureContextIsSetFrom(context)
    }
}