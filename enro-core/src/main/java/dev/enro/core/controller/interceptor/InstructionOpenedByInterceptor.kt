package dev.enro.core.controller.interceptor

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.EnroException
import dev.enro.core.NavigationBinding
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationKey
import dev.enro.core.readOpenInstruction

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
        val binding =  parentContext.controller.bindingForKeyType(navigationKey::class)
            ?: throw EnroException.MissingNavigationBinding(navigationKey)
        return internal.copy(
            openingType = binding.destinationType.java
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