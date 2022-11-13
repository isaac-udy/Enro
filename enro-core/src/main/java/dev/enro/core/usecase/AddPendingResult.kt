package dev.enro.core.usecase

import dev.enro.core.NavigationContext
import dev.enro.core.NavigationInstruction

internal interface AddPendingResult {
    operator fun invoke(
        navigationContext: NavigationContext<*>,
        instruction: NavigationInstruction.Close
    )
}