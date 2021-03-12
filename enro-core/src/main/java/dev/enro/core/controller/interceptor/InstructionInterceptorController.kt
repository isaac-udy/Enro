package dev.enro.core.controller.interceptor

import dev.enro.core.NavigationContext
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.Navigator

class InstructionInterceptorController(
    private val interceptors: List<NavigationInstructionInterceptor>
) {

    fun intercept(
        instruction: NavigationInstruction.Open,
        parentContext: NavigationContext<*>,
        navigator: Navigator<out NavigationKey, out Any>
    ): NavigationInstruction.Open {
        return interceptors.fold(instruction) { acc, interceptor ->
            interceptor.intercept(acc, parentContext, navigator)
        }
    }
}