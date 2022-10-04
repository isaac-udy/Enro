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
            .setOpenTarget(parentContext)
            .setOpenExecutedBy(parentContext)
            .setOpenRequestedBy(parentContext)
    }

    private fun AnyOpenInstruction.setOpenTarget(
        parentContext: NavigationContext<*>
    ) : AnyOpenInstruction {
        if (internal.openTarget != Any::class.java) return internal
        return internal.copy(
            openTarget = parentContext.controller.navigatorForKeyType(navigationKey::class)!!.contextType.java
        )
    }

    private fun AnyOpenInstruction.setOpenRequestedBy(
        parentContext: NavigationContext<*>
    ): AnyOpenInstruction {
        // If openRequestedBy has been set, don't change it
        if(internal.openRequestedBy != Any::class.java) return internal
        return internal.copy(openRequestedBy = parentContext.contextReference::class.java)
    }

    private fun AnyOpenInstruction.setOpenExecutedBy(
        parentContext: NavigationContext<*>
    ): AnyOpenInstruction {
        return internal.copy(openExecutedBy = parentContext.contextReference::class.java)
    }
}