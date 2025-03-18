package dev.enro.core.result

import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationKey
import dev.enro.core.getNavigationHandle
import dev.enro.core.synthetic.SyntheticDestination
import dev.enro.core.synthetic.SyntheticDestinationScope

public fun <T : Any> SyntheticDestination<out NavigationKey.WithResult<T>>.sendResult(
    result: T
) {
    AdvancedResultExtensions.setResultForInstruction(
        navigationContext.controller,
        instruction,
        result
    )
}

public fun <T : Any> SyntheticDestinationScope<out NavigationKey.WithResult<T>>.sendResult(
    result: T
) {
    destination.sendResult(result)
}

public fun <T : Any> SyntheticDestination<out NavigationKey.WithResult<T>>.forwardResult(
    navigationKey: NavigationKey.WithResult<T>,
) {
    navigationContext.getNavigationHandle().executeInstruction(
        AdvancedResultExtensions
            .getInstructionToForwardResult(
                originalInstruction = instruction,
                direction = NavigationDirection.defaultDirection(navigationKey),
                navigationKey = navigationKey,
            ).apply {
                // Synthetic destinations don't really "exist" in the graph, so we only want to pass-on the forwarding
                // result id if the synthetic instruction itself had a forwarding result id, rather than
                // begin a new forwarding chain like we would for a "normal" forwarding operation
                if(extras[AdvancedResultExtensions.FORWARDING_RESULT_FROM_EXTRA] == instruction.instructionId) {
                    extras.remove(AdvancedResultExtensions.FORWARDING_RESULT_FROM_EXTRA)
                }
            }
    )
}

public fun <T : Any> SyntheticDestinationScope<out NavigationKey.WithResult<T>>.forwardResult(
    navigationKey: NavigationKey.WithResult<T>
) {
    destination.forwardResult(navigationKey)
}
