package dev.enro.core.controller.usecase

import dev.enro.core.NavigationContext
import dev.enro.core.NavigationInstruction
import dev.enro.core.container.DefaultContainerExecutor
import dev.enro.core.controller.repository.InstructionInterceptorRepository
import dev.enro.core.getNavigationHandle

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

        val executor = DefaultContainerExecutor
        executor.close(navigationContext)
        addPendingResult(navigationContext, processedInstruction)
    }
}