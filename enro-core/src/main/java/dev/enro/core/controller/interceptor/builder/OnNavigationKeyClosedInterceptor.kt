package dev.enro.core.controller.interceptor.builder

import dev.enro.core.NavigationContext
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.controller.interceptor.NavigationInstructionInterceptor
import dev.enro.core.readOpenInstruction

@PublishedApi
internal class OnNavigationKeyClosedInterceptor(
    private val matcher: (NavigationKey) -> Boolean,
    private val action: (NavigationKey) -> InterceptorBehavior
) : NavigationInstructionInterceptor {
    override fun intercept(
        instruction: NavigationInstruction.Close,
        context: NavigationContext<*>,
    ): NavigationInstruction? {
        val openInstruction = context.arguments.readOpenInstruction() ?: return instruction
        if(!matcher(openInstruction.navigationKey)) return openInstruction
        val result = action(openInstruction.navigationKey)
        return when(result) {
            InterceptorBehavior.Cancel -> null
            InterceptorBehavior.Continue -> instruction
            is InterceptorBehavior.ReplaceWith -> result.instruction
        }
    }
}