package dev.enro.core.internal.handle

import dev.enro.core.NavigationInstruction
import dev.enro.core.controller.NavigationController

internal class TestNavigationHandleViewModel(
    controller: NavigationController,
    instruction: NavigationInstruction.Open
) : NavigationHandleViewModel(controller, instruction) {

    private val instructions = mutableListOf<NavigationInstruction>()

    override fun executeInstruction(navigationInstruction: NavigationInstruction) {
        instructions.add(navigationInstruction)
    }
}