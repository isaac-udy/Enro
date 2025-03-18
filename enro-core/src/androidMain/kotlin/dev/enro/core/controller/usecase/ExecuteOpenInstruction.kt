package dev.enro.core.controller.usecase

import dev.enro.core.*
import dev.enro.core.container.DefaultContainerExecutor
import dev.enro.core.controller.repository.InstructionInterceptorRepository
import dev.enro.core.controller.repository.NavigationBindingRepository

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

        DefaultContainerExecutor.open(
            fromContext = navigationContext,
            binding = binding,
            instruction = processedInstruction,
        )
    }
}