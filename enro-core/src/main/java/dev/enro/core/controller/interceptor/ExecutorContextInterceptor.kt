package dev.enro.core.controller.interceptor

import dev.enro.core.*
import dev.enro.core.hosts.AbstractSingleFragmentKey
import dev.enro.core.hosts.SingleFragmentActivity

internal class ExecutorContextInterceptor : NavigationInstructionInterceptor{

    override fun intercept(
        instruction: AnyOpenInstruction,
        parentContext: NavigationContext<*>,
        navigator: Navigator<out NavigationKey, out Any>
    ): AnyOpenInstruction {
        return instruction
            .setExecutorContext(parentContext)
    }

    private fun AnyOpenInstruction.setExecutorContext(
        parentContext: NavigationContext<*>
    ): AnyOpenInstruction {
        // If the executor context has been set, don't change it
        if(internal.executorContext != null) return internal

        if(parentContext.contextReference is SingleFragmentActivity) {
            val singleFragmentKey = parentContext.getNavigationHandle().asTyped<AbstractSingleFragmentKey>().key
            if(instructionId == singleFragmentKey.instruction.instructionId) {
                return internal
            }
        }
        return internal.copy(executorContext = parentContext.contextReference::class.java)
    }
}