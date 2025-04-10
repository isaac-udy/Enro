package dev.enro.core.controller.usecase

import dev.enro.core.NavigationContext
import dev.enro.core.NavigationInstruction
import dev.enro.core.container.close
import dev.enro.core.container.setBackstack
import dev.enro.core.controller.repository.InstructionInterceptorRepository
import dev.enro.core.getNavigationHandle
import dev.enro.core.parentContainer
import dev.enro.core.toDisplayString

internal interface ExecuteCloseInstruction {
    operator fun invoke(
        navigationContext: NavigationContext<out Any>,
        instruction: NavigationInstruction.Close
    )
}

internal class ExecuteCloseInstructionImpl(
    private val addPendingResult: AddPendingResult,
    private val interceptorRepository: InstructionInterceptorRepository
): ExecuteCloseInstruction {

    override operator fun invoke(
        navigationContext: NavigationContext<out Any>,
        instruction: NavigationInstruction.Close,
    ) {
        val processedInstruction = interceptorRepository.intercept(
            instruction, navigationContext
        ) ?: return

        if (processedInstruction !is NavigationInstruction.Close) {
            navigationContext.getNavigationHandle().executeInstruction(processedInstruction)
            return
        }

        val container = requireNotNull(navigationContext.parentContainer()) {
            "Failed to close navigation context ${navigationContext.toDisplayString()} in context because it has no parent container"
        }
        container.setBackstack {
            it.close(navigationContext.getNavigationHandle().id)
        }
        addPendingResult(navigationContext, processedInstruction)
        if (instruction is NavigationInstruction.Close.AndThenOpen) {
            container.context.navigationHandle.executeInstruction(instruction.instruction)
        }
    }
}