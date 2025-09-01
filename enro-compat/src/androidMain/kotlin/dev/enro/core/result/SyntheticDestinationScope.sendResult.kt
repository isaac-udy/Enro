package dev.enro.core.result

import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.core.NavigationDirection
import dev.enro.core.getNavigationHandle
import dev.enro.ui.destinations.SyntheticDestinationScope

public fun <T: Any> SyntheticDestinationScope<out NavigationKey.WithResult<T>>.sendResult(
    result: T
) {
    AdvancedResultExtensions.setResultForInstruction(
        navigationController = context.controller,
        instruction = instance,
        result = result
    )
}

public fun <T: Any> SyntheticDestinationScope<out NavigationKey.WithResult<T>>.forwardResult(
    navigationKey: NavigationKey.WithResult<T>,
    direction: NavigationDirection = when(navigationKey) {
        is dev.enro.core.NavigationKey.SupportsPresent -> NavigationDirection.Present
        else -> NavigationDirection.Push
    }
) {
    val instruction = AdvancedResultExtensions.getInstructionToForwardResult(
        originalInstruction = instance,
        direction = direction,
        navigationKey = navigationKey
    )
    context.getNavigationHandle().execute(NavigationOperation.Open(instruction))
}