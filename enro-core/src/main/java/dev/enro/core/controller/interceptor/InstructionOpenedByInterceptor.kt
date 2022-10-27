package dev.enro.core.controller.interceptor

import dev.enro.core.*

internal object InstructionOpenedByInterceptor : NavigationInstructionInterceptor {

    override fun intercept(
        instruction: AnyOpenInstruction,
        context: NavigationContext<*>,
        binding: NavigationBinding<out NavigationKey, out Any>
    ): AnyOpenInstruction {
        return instruction
            .setOpeningType(context)
            .setOpenedBy(context)
    }

    private fun AnyOpenInstruction.setOpeningType(
        parentContext: NavigationContext<*>
    ) : AnyOpenInstruction {
        if (internal.openingType != Any::class.java) return internal
        return internal.copy(
            openingType = parentContext.controller.bindingForKeyType(navigationKey::class)!!.destinationType.java
        )
    }

    private fun AnyOpenInstruction.setOpenedBy(
        parentContext: NavigationContext<*>
    ): AnyOpenInstruction {
        // If openRequestedBy has been set, don't change it
        if(internal.openedByType != Any::class.java) return internal
        return internal.copy(
            openedByType = parentContext.contextReference::class.java,
            openedById = parentContext.arguments.readOpenInstruction()?.instructionId
        )
    }
}