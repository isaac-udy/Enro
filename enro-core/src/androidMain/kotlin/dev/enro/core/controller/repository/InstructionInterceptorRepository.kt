package dev.enro.core.controller.repository

import dev.enro.core.*
import dev.enro.core.controller.interceptor.InstructionOpenedByInterceptor
import dev.enro.core.controller.interceptor.NavigationInstructionInterceptor

internal class InstructionInterceptorRepository {

    private val interceptors: MutableList<NavigationInstructionInterceptor> = mutableListOf()

    fun addInterceptors(interceptors: List<NavigationInstructionInterceptor>) {
        this.interceptors.addAll(interceptors)
    }

    fun intercept(
        instruction: AnyOpenInstruction,
        parentContext: NavigationContext<*>,
        binding: NavigationBinding<out NavigationKey, out Any>
    ): AnyOpenInstruction? {
        return (interceptors + InstructionOpenedByInterceptor).fold(instruction) { acc, interceptor ->
            val result = interceptor.intercept(acc, parentContext, binding)

            when (result) {
                is NavigationInstruction.Open<*> -> {
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
                is NavigationInstruction.Open<*> -> {
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