package dev.enro.core.controller.interceptor.builder

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationKey
import dev.enro.core.controller.interceptor.NavigationInstructionInterceptor

@PublishedApi
internal class OnNavigationKeyOpenedInterceptor(
    private val matcher: (NavigationKey) -> Boolean,
    private val action: (NavigationKey) -> InterceptorBehavior
) : NavigationInstructionInterceptor {
    override fun intercept(
        instruction: AnyOpenInstruction,
        context: NavigationContext<*>,
        binding: NavigationBinding<out NavigationKey, out Any>
    ): AnyOpenInstruction? {
        if(!matcher(instruction.navigationKey)) return instruction
        val result = action(instruction.navigationKey)
        return when(result) {
            InterceptorBehavior.Cancel -> null
            InterceptorBehavior.Continue -> instruction
            is InterceptorBehavior.ReplaceWith -> result.instruction
        }
    }
}