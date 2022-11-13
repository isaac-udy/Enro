package dev.enro.core.controller.usecase

import dev.enro.core.*
import dev.enro.core.controller.repository.InstructionInterceptorRepository
import dev.enro.core.usecase.AddPendingResult
import dev.enro.core.usecase.ExecuteCloseInstruction
import dev.enro.core.usecase.GetNavigationExecutor

internal class ExecuteCloseInstructionImpl(
    private val addPendingResult: AddPendingResult,
    private val getNavigationExecutor: GetNavigationExecutor,
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

        addPendingResult(navigationContext, instruction)

        val executor: NavigationExecutor<Any, Any, NavigationKey> = getNavigationExecutor(
            navigationContext.getNavigationHandle().instruction.internal.openedByType to navigationContext.contextReference::class.java
        )
        executor.preClosed(navigationContext)
        executor.close(navigationContext)
    }
}