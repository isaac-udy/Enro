package dev.enro.core.controller.usecase

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContext
import dev.enro.core.readOpenInstruction
import dev.enro.core.usecase.EnsureNavigationInstructionHasContext

internal class EnsureNavigationInstructionHasContextImpl : EnsureNavigationInstructionHasContext {

    override fun invoke(instruction: AnyOpenInstruction, context: NavigationContext<*>): AnyOpenInstruction {
        return instruction
            .setOpeningType(context)
            .setOpenedBy(context)
    }

    private fun AnyOpenInstruction.setOpeningType(
        parentContext: NavigationContext<*>
    ) : AnyOpenInstruction {
        if (internal.openingType != kotlin.Any::class.java) return internal
        return internal.copy(
            openingType = parentContext.controller.bindingForKeyType(navigationKey::class)!!.destinationType.java
        )
    }

    private fun AnyOpenInstruction.setOpenedBy(
        parentContext: NavigationContext<*>
    ): AnyOpenInstruction {
        // If openRequestedBy has been set, don't change it
        if(internal.openedByType != kotlin.Any::class.java) return internal
        return internal.copy(
            openedByType = parentContext.contextReference::class.java,
            openedById = parentContext.arguments.readOpenInstruction()?.instructionId
        )
    }
}