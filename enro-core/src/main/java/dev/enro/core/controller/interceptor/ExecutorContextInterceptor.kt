package dev.enro.core.controller.interceptor

import dev.enro.core.*
import dev.enro.core.hosts.AbstractOpenInstructionInActivityKey
import dev.enro.core.hosts.ActivityHostForAnyInstruction

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

        if(parentContext.contextReference is ActivityHostForAnyInstruction) {
            val openActivityKey = parentContext.getNavigationHandle().asTyped<AbstractOpenInstructionInActivityKey>().key
            if(instructionId == openActivityKey.instruction.instructionId) {
                return internal
            }
        }
        return internal.copy(executorContext = parentContext.contextReference::class.java)
    }
}