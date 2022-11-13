package dev.enro.core.controller.usecase

import dev.enro.core.NavigationContext
import dev.enro.core.NavigationInstruction
import dev.enro.core.readOpenInstruction
import dev.enro.core.result.ResultChannelId
import dev.enro.core.result.internal.EnroResult
import dev.enro.core.result.internal.PendingResult
import dev.enro.core.usecase.AddPendingResult

internal class AddPendingResultImpl(
    private val enroResult: EnroResult,
) : AddPendingResult {
    override operator fun invoke(
        navigationContext: NavigationContext<*>,
        instruction: NavigationInstruction.Close
    ) {
        if (instruction !is NavigationInstruction.Close.WithResult) return
        val openInstruction = navigationContext.arguments.readOpenInstruction() ?: return
        val resultId = openInstruction.internal.resultId ?: when {
            navigationContext.controller.isInTest ->  ResultChannelId(
                ownerId = openInstruction.instructionId,
                resultId = openInstruction.instructionId
            )
            else -> return
        }
        enroResult.addPendingResult(
            PendingResult(
                resultChannelId = resultId,
                resultType = instruction.result::class,
                result = instruction.result,
            )
        )
    }
}