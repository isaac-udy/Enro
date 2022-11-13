package dev.enro.core.usecase

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContext
import dev.enro.core.get

internal interface EnsureNavigationInstructionHasContext {
    operator fun invoke(
        instruction: AnyOpenInstruction,
        context: NavigationContext<*>,
    ): AnyOpenInstruction
}

internal fun AnyOpenInstruction.ensureContextIsSetFrom(navigationContext: NavigationContext<*>): AnyOpenInstruction {
    return navigationContext.controller.dependencyScope.get<EnsureNavigationInstructionHasContext>().invoke(
        instruction = this,
        context = navigationContext
    )
}