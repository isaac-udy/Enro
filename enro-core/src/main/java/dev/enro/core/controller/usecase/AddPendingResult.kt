package dev.enro.core.controller.usecase

import dev.enro.core.NavigationContext
import dev.enro.core.NavigationInstruction
import dev.enro.core.controller.NavigationController
import dev.enro.core.readOpenInstruction
import dev.enro.core.result.EnroResult
import dev.enro.core.result.internal.PendingResult
import dev.enro.core.result.internal.ResultChannelId

internal class AddPendingResult(
    private val controller: NavigationController,
    private val enroResult: EnroResult,
) {
    operator fun invoke(
        navigationContext: NavigationContext<*>,
        instruction: NavigationInstruction.Close
    ) {
        val openInstruction = navigationContext.arguments.readOpenInstruction() ?: return
        val resultId = openInstruction.internal.resultId ?: when {
            controller.isInTest -> ResultChannelId(
                ownerId = openInstruction.instructionId,
                resultId = openInstruction.instructionId
            )
            else -> return
        }
        when(instruction) {
            NavigationInstruction.Close -> enroResult.addPendingResult(
                PendingResult.Closed(
                    resultChannelId = resultId
                )
            )
            is NavigationInstruction.Close.WithResult -> enroResult.addPendingResult(
                PendingResult.Result(
                    resultChannelId = resultId,
                    resultType = instruction.result::class,
                    result = instruction.result,
                )
            )
        }
    }
}