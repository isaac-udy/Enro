package dev.enro.core.result

import dev.enro.core.AdvancedEnroApi
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.controller.NavigationController
import dev.enro.core.result.internal.PendingResult

@AdvancedEnroApi
public object AdvancedResultExtensions {

    public fun getForwardingInstructionId(instruction: NavigationInstruction.Open<*>) : String? {
        return instruction.extras[FORWARDING_RESULT_FROM_EXTRA] as? String
    }

    public fun <T : NavigationDirection> getInstructionToForwardResult(
        originalInstruction: NavigationInstruction.Open<*>,
        direction: T,
        navigationKey: NavigationKey.WithResult<*>,
    ) : NavigationInstruction.Open<T> {
        return NavigationInstruction.Open.OpenInternal(
            navigationDirection = direction,
            navigationKey = navigationKey,
            resultId = originalInstruction.internal.resultId,
            resultKey = originalInstruction.internal.resultKey
                ?: originalInstruction.navigationKey
        ).apply {
            extras[FORWARDING_RESULT_FROM_EXTRA] = originalInstruction.extras[FORWARDING_RESULT_FROM_EXTRA]
                ?: originalInstruction.instructionId
        }
    }

    @AdvancedEnroApi
    public fun <T : Any> setResultForInstruction(
        navigationController: NavigationController,
        instruction: NavigationInstruction.Open<*>,
        result: T
    ) {
        val resultId = instruction.internal.resultId
        if (resultId != null) {
            val keyForResult = instruction.internal.resultKey
                ?: instruction.navigationKey
            if (keyForResult !is NavigationKey.WithResult<*>) return

            EnroResult.from(navigationController).addPendingResult(
                PendingResult.Result(
                    resultChannelId = resultId,
                    navigationKey = keyForResult,
                    resultType = result::class,
                    result = result
                )
            )
        }
    }

    @AdvancedEnroApi
    public fun setClosedResultForInstruction(
        navigationController: NavigationController,
        instruction: NavigationInstruction.Open<*>,
    ) {
        val resultId = instruction.internal.resultId
        if (resultId != null) {
            val keyForResult = instruction.internal.resultKey
                ?: instruction.navigationKey
            if (keyForResult !is NavigationKey.WithResult<*>) return

            EnroResult.from(navigationController).addPendingResult(
                PendingResult.Closed(
                    resultChannelId = resultId,
                    navigationKey = keyForResult,
                )
            )
        }
    }

    internal const val FORWARDING_RESULT_FROM_EXTRA = "AdvancedResultExtensions.FORWARDING_RESULT_EXTRA"
}