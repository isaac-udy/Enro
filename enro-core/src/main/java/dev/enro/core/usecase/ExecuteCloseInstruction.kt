package dev.enro.core.usecase

import dev.enro.core.NavigationContext
import dev.enro.core.NavigationInstruction

internal interface ExecuteCloseInstruction {
    operator fun invoke(
        navigationContext: NavigationContext<out Any>,
        instruction: NavigationInstruction.Close
    )
}