package dev.enro.core.controller.interceptor.builder

import dev.enro.core.*
import dev.enro.core.controller.interceptor.NavigationInstructionInterceptor

internal class AggregateNavigationInstructionInterceptor(
    private val interceptors: List<NavigationInstructionInterceptor>
) : NavigationInstructionInterceptor {
    override fun intercept(
        instruction: AnyOpenInstruction,
        context: NavigationContext<*>,
        binding: NavigationBinding<out NavigationKey, out Any>
    ): AnyOpenInstruction? {
        return interceptors.fold<NavigationInstructionInterceptor, AnyOpenInstruction?>(instruction) { interceptedInstruction, interceptor ->
            if(interceptedInstruction == null) return null
            interceptor.intercept(
                interceptedInstruction,
                context,
                binding
            )
        }
    }

    override fun intercept(
        instruction: NavigationInstruction.Close,
        context: NavigationContext<*>
    ): NavigationInstruction? {
        return interceptors.fold<NavigationInstructionInterceptor, NavigationInstruction?>(instruction) { interceptedInstruction, interceptor ->
            if(interceptedInstruction !is NavigationInstruction.Close) return interceptedInstruction
            interceptor.intercept(
                interceptedInstruction,
                context,
            )
        }
    }
}