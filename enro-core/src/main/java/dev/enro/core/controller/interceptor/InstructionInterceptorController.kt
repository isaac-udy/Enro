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
    ): NavigationInstruction.Open? {
        return interceptors.fold(instruction) { acc, interceptor ->
            val result = interceptor.intercept(acc, parentContext, navigator)

            when (result) {
                is NavigationInstruction.Open -> {
                    return@fold result
                }
                else -> return null
            }
        }
    }

    fun intercept(
        instruction: NavigationInstruction.Close,
        context: NavigationContext<*>
    ): NavigationInstruction? {
        return interceptors.fold(instruction) { acc, interceptor ->
            val result = interceptor.intercept(acc, context)

            when (result) {
                is NavigationInstruction.Open -> {
                    return result
                }
                is NavigationInstruction.Close -> {
                    return@fold result
                }
                else -> return null
            }
        }
    }
}