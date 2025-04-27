package dev.enro.core.controller.usecase

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.EnroException
import dev.enro.core.NavigationContext
import dev.enro.core.container.findContainerFor
import dev.enro.core.container.setBackstack
import dev.enro.core.controller.repository.InstructionInterceptorRepository
import dev.enro.core.controller.repository.NavigationBindingRepository
import dev.enro.core.getNavigationHandle

internal interface ExecuteOpenInstruction {
    operator fun invoke(
        navigationContext: NavigationContext<out Any>,
        instruction: AnyOpenInstruction
    )
}

internal class ExecuteOpenInstructionImpl(
    private val bindingRepository: NavigationBindingRepository,
    private val interceptorRepository: InstructionInterceptorRepository
) : ExecuteOpenInstruction {
    override operator fun invoke(
        navigationContext: NavigationContext<out Any>,
        instruction: AnyOpenInstruction
    ) {
        val binding = bindingRepository.bindingForInstruction(instruction)
            ?: throw EnroException.MissingNavigationBinding(instruction.navigationKey)

        val processedInstruction = interceptorRepository.intercept(
            instruction, navigationContext, binding
        ) ?: return

        if (processedInstruction.navigationKey::class != binding.keyType) {
            navigationContext.getNavigationHandle().executeInstruction(processedInstruction)
            return
        }

        val container = findContainerFor(navigationContext, processedInstruction)
        // TODO: Do we need warnings here for non-hosted instructions/instructions that are going to be opened in a new window?
        if (container != null) {
            container.setBackstack { backstack ->
                backstack.plus(processedInstruction)
            }
        } else {
            val controller = navigationContext.controller
            controller.windowManager.open(processedInstruction)
        }
    }
}