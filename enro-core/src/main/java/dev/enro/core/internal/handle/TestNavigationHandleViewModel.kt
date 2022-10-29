package dev.enro.core.internal.handle

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationInstruction
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.usecase.ExecuteCloseInstruction
import dev.enro.core.controller.usecase.ExecuteOpenInstruction

internal class TestNavigationHandleViewModel(
    controller: NavigationController,
    instruction: AnyOpenInstruction
) : NavigationHandleViewModel(
    instruction = instruction,
    dependencyScope = NavigationHandleScope(controller),
    executeOpenInstruction = object: ExecuteOpenInstruction {
        override fun invoke(
            navigationContext: NavigationContext<out Any>,
            instruction: AnyOpenInstruction
        ) {}
    },
    executeCloseInstruction = object : ExecuteCloseInstruction {
        override fun invoke(
            navigationContext: NavigationContext<out Any>,
            instruction: NavigationInstruction.Close
        ) {}
    },
) {
    private val instructions = mutableListOf<NavigationInstruction>()

    override fun executeInstruction(navigationInstruction: NavigationInstruction) {
        instructions.add(navigationInstruction)
    }
}