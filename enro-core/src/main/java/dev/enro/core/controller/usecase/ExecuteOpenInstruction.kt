package dev.enro.core.controller.usecase

import dev.enro.core.*
import dev.enro.core.controller.repository.InstructionInterceptorRepository
import dev.enro.core.controller.repository.NavigationBindingRepository

internal interface ExecuteOpenInstruction {
    operator fun invoke(
        navigationContext: NavigationContext<out Any>,
        instruction: AnyOpenInstruction
    )
}

internal class ExecuteOpenInstructionImpl(
    private val getNavigationExecutor: GetNavigationExecutor,
    private val bindingRepository: NavigationBindingRepository,
    private val interceptorRepository: InstructionInterceptorRepository
): ExecuteOpenInstruction {
    override operator fun invoke(
        navigationContext: NavigationContext<out Any>,
        instruction: AnyOpenInstruction
    ) {
        val binding = bindingRepository.bindingForKeyType(instruction.navigationKey::class)
            ?: throw EnroException.MissingNavigationBinding("Attempted to execute $instruction but could not find a valid navigation binding for the key type on this instruction")

        val processedInstruction = interceptorRepository.intercept(
            instruction, navigationContext, binding
        ) ?: return

        if (processedInstruction.navigationKey::class != binding.keyType) {
            navigationContext.getNavigationHandle().executeInstruction(processedInstruction)
            return
        }
        val executor = getNavigationExecutor(
            processedInstruction.internal.openedByType to processedInstruction.internal.openingType
        )

        val args = ExecutorArgs(
            navigationContext,
            binding,
            processedInstruction.navigationKey,
            processedInstruction
        )

        executor.preOpened(navigationContext)
        executor.open(args)
    }
}