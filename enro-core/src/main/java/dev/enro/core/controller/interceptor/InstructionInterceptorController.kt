package dev.enro.core.controller.interceptor

import nav.enro.core.NavigationContext
import nav.enro.core.NavigationInstruction
import nav.enro.core.NavigationKey
import nav.enro.core.Navigator

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