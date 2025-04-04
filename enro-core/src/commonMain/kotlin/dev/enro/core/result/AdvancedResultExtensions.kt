package dev.enro.core.result

import androidx.savedstate.read
import androidx.savedstate.write
import dev.enro.annotations.AdvancedEnroApi
import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationInstruction
import dev.enro.core.NavigationKey
import dev.enro.core.controller.NavigationController
import dev.enro.core.result.internal.PendingResult

@AdvancedEnroApi
public object AdvancedResultExtensions {

    public fun getForwardingInstructionId(instruction: NavigationInstruction.Open<*>): String? {
        return instruction.extras.read {
            if (!contains(FORWARDING_RESULT_FROM_EXTRA)) return null
            getString(FORWARDING_RESULT_FROM_EXTRA)
        }
    }

    public fun <T : NavigationDirection> getInstructionToForwardResult(
        originalInstruction: NavigationInstruction.Open<*>,
        direction: T,
        navigationKey: NavigationKey.WithResult<*>,
    ): NavigationInstruction.Open<T> {
        return NavigationInstruction.Open.OpenInternal(
            navigationDirection = direction,
            navigationKey = navigationKey,
            resultId = originalInstruction.internal.resultId,
            resultKey = originalInstruction.internal.resultKey
                ?: originalInstruction.navigationKey
        ).apply {
            extras.write {
                val originalForwardingInstructionId = originalInstruction.extras.read {
                    if (!contains(FORWARDING_RESULT_FROM_EXTRA)) return@read null
                    getString(FORWARDING_RESULT_FROM_EXTRA)
                }
                putString(
                    FORWARDING_RESULT_FROM_EXTRA,
                    originalForwardingInstructionId ?: originalInstruction.instructionId,
                )
            }
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
                    instruction = instruction,
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
                    instruction = instruction,
                    navigationKey = keyForResult,
                )
            )
        }
    }

    internal const val FORWARDING_RESULT_FROM_EXTRA = "AdvancedResultExtensions.FORWARDING_RESULT_EXTRA"
}