package dev.enro.core.controller.usecase

import dev.enro.core.NavigationContext
import dev.enro.core.NavigationInstruction
import dev.enro.core.findContainer

internal interface ExecuteContainerOperationInstruction {
    operator fun invoke(
        navigationContext: NavigationContext<out Any>,
        instruction: NavigationInstruction.ContainerOperation
    )
}

internal class ExecuteContainerOperationInstructionImpl(): ExecuteContainerOperationInstruction {
    override operator fun invoke(
        navigationContext: NavigationContext<out Any>,
        instruction: NavigationInstruction.ContainerOperation
    ) {
        val container = navigationContext
            .findContainer(instruction.containerKey)
        requireNotNull(container)
        instruction.operation.invoke(container)
    }
}