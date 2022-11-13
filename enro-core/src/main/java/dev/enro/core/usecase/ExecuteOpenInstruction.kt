package dev.enro.core.usecase

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContext

internal interface ExecuteOpenInstruction {
    operator fun invoke(
        navigationContext: NavigationContext<out Any>,
        instruction: AnyOpenInstruction
    )
}