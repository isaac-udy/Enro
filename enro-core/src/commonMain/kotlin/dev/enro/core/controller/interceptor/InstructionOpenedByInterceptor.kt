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
        if (openingType != null) return this
        val binding =  parentContext.controller.bindingForInstruction(this)
            ?: throw EnroException.MissingNavigationBinding(navigationKey)
        return copy(
            openingType = binding.destinationType.qualifiedName
        )
    }

    private fun AnyOpenInstruction.setOpenedBy(
        parentContext: NavigationContext<*>
    ): AnyOpenInstruction {
        // If openRequestedBy has been set, don't change it
        if(openedByType != null) return this
        return copy(
            openedByType = parentContext.contextReference::class.qualifiedName,
            openedById = parentContext.arguments.readOpenInstruction()?.instructionId
        )
    }
}