package dev.enro.core.controller.usecase

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.EnroException
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationDirection
import dev.enro.core.container.asPresentInstruction
import dev.enro.core.container.findContainerFor
import dev.enro.core.container.setBackstack
import dev.enro.core.controller.repository.InstructionInterceptorRepository
import dev.enro.core.controller.repository.NavigationBindingRepository
import dev.enro.core.getNavigationHandle
import dev.enro.core.readOpenInstruction

internal interface ExecuteOpenInstruction {
    operator fun invoke(
        navigationContext: NavigationContext<out Any>,
        instruction: AnyOpenInstruction
    )
}

internal class ExecuteOpenInstructionImpl(
    private val bindingRepository: NavigationBindingRepository,
    private val interceptorRepository: InstructionInterceptorRepository
): ExecuteOpenInstruction {
    override operator fun invoke(
        navigationContext: NavigationContext<out Any>,
        instruction: AnyOpenInstruction
    ) {
        val binding = bindingRepository.bindingForKeyType(instruction.navigationKey::class)
            ?: throw EnroException.MissingNavigationBinding(instruction.navigationKey)

        val processedInstruction = interceptorRepository.intercept(
            instruction, navigationContext, binding
        ) ?: return

        if (processedInstruction.navigationKey::class != binding.keyType) {
            navigationContext.getNavigationHandle().executeInstruction(processedInstruction)
            return
        }

        val container = findContainerFor(navigationContext, processedInstruction)
        requireNotNull(container) {
            "Failed to execute instruction from context with NavigationKey ${navigationContext.arguments.readOpenInstruction()!!.navigationKey::class.simpleName}: Could not find valid container for NavigationKey of type ${instruction.navigationKey::class.simpleName}"
        }
        container.setBackstack { backstack ->
            backstack.plus(processedInstruction)
        }
    }
}