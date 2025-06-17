package dev.enro.core.result

import dev.enro.NavigationKey
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.asInstance
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationInstructionOpen
import dev.enro.core.controller.NavigationController
import dev.enro.core.setNavigationDirection
import dev.enro.result.NavigationResult
import dev.enro.result.NavigationResultChannel

@AdvancedEnroApi
public object AdvancedResultExtensions {

    @AdvancedEnroApi
    @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
    public fun getForwardingInstructionId(instruction: NavigationInstructionOpen): String? {
        val resultId = instruction.metadata.get(NavigationResultChannel.ResultIdKey)
        return resultId?.ownerId?.takeIf { it != instruction.id }
    }

    @AdvancedEnroApi
    @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
    public fun <T : NavigationDirection> getInstructionToForwardResult(
        originalInstruction: NavigationInstructionOpen,
        direction: T,
        navigationKey: NavigationKey.WithResult<*>,
    ): NavigationInstructionOpen {
        val originalResultId = originalInstruction.metadata.get(NavigationResultChannel.ResultIdKey)
        val instruction = navigationKey.asInstance()
        instruction.setNavigationDirection(direction)
        instruction.metadata.set(NavigationResultChannel.ResultIdKey, originalResultId)
        return instruction
    }

    @AdvancedEnroApi
    @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
    public fun <T : Any> setResultForInstruction(
        navigationController: NavigationController,
        instruction: NavigationInstructionOpen,
        result: T
    ) {
        NavigationResultChannel.registerResult(
            NavigationResult.Completed(
                instruction,
                result
            )
        )
    }

    @AdvancedEnroApi
    @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
    public fun setClosedResultForInstruction(
        navigationController: NavigationController,
        instruction: NavigationInstructionOpen,
    ) {
        NavigationResultChannel.registerResult(
            NavigationResult.Closed(
                instruction,
            )
        )
    }
}