package dev.enro.core.controller.interceptor.builder

import dev.enro.core.NavigationContext
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.controller.interceptor.NavigationInstructionInterceptor
import dev.enro.core.readOpenInstruction

@PublishedApi
internal class OnNavigationKeyClosedWithResultInterceptor<T: Any>(
    private val matcher: (NavigationKey) -> Boolean,
    private val action: (NavigationKey, T) -> InterceptorBehavior
) : NavigationInstructionInterceptor {
    override fun intercept(
        instruction: NavigationInstruction.Close,
        context: NavigationContext<*>,
    ): NavigationInstruction? {
        if(instruction !is NavigationInstruction.Close.WithResult) return instruction
        val openInstruction = context.arguments.readOpenInstruction() ?: return instruction
        if(!matcher(openInstruction.navigationKey)) return openInstruction

        // This should be checked by reified types when this interceptor is constructed
        @Suppress("UNCHECKED_CAST")
        val result = action(openInstruction.navigationKey, instruction.result as T)

        return when(result) {
            InterceptorBehavior.Cancel -> null
            InterceptorBehavior.Continue -> instruction
            is InterceptorBehavior.ReplaceWith -> result.instruction
        }
    }
}