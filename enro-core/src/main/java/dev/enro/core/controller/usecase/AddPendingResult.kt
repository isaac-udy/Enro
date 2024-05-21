package dev.enro.core.controller.usecase

import dev.enro.core.NavigationContext
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.controller.NavigationController
import dev.enro.core.result.AdvancedResultExtensions
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
        val openInstruction = navigationContext.instruction
        val navigationKey = openInstruction.internal.resultKey
            ?: openInstruction.navigationKey

        if (navigationKey !is NavigationKey.WithResult<*>) return
        val resultId = openInstruction.internal.resultId ?: when {
            controller.isInTest -> ResultChannelId(
                ownerId = openInstruction.instructionId,
                resultId = openInstruction.instructionId
            )

            else -> return
        }
        when (instruction) {
            NavigationInstruction.Close -> {
                // If this instruction is forwarding a result from another instruction,
                // we don't want this instruction to actually deliver the close result, as only
                // the original instruction should deliver a close
                if (AdvancedResultExtensions.getForwardingInstructionId(openInstruction) != null) return
                enroResult.addPendingResult(
                    PendingResult.Closed(
                        resultChannelId = resultId,
                        instruction = navigationContext.instruction,
                        navigationKey = navigationKey,
                    )
                )
            }

            is NavigationInstruction.Close.WithResult -> enroResult.addPendingResult(
                PendingResult.Result(
                    resultChannelId = resultId,
                    instruction = navigationContext.instruction,
                    navigationKey = navigationKey,
                    resultType = instruction.result::class,
                    result = instruction.result,
                )
            )
        }
    }
}