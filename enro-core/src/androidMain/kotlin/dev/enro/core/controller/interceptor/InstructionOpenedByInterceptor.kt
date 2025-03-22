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
        if (internal.openingType != Any::class) return internal
        val binding =  parentContext.controller.bindingForKeyType(navigationKey::class)
            ?: throw EnroException.MissingNavigationBinding(navigationKey)
        return internal.copy(
            openingType = binding.destinationType
        )
    }

    private fun AnyOpenInstruction.setOpenedBy(
        parentContext: NavigationContext<*>
    ): AnyOpenInstruction {
        // If openRequestedBy has been set, don't change it
        if(internal.openedByType != Any::class) return internal
        return internal.copy(
            openedByType = parentContext.contextReference::class,
            openedById = parentContext.arguments.readOpenInstruction()?.instructionId
        )
    }
}