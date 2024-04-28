package dev.enro.core.controller.usecase

import dev.enro.core.NavigationContext
import dev.enro.core.NavigationInstruction
import dev.enro.core.findContainer
import dev.enro.core.parentContainer

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
        val container = when(instruction.target) {
            NavigationInstruction.ContainerOperation.Target.ParentContainer -> navigationContext.parentContainer()
            NavigationInstruction.ContainerOperation.Target.ActiveContainer -> navigationContext.containerManager.activeContainer
            is NavigationInstruction.ContainerOperation.Target.TargetContainer -> navigationContext.findContainer(instruction.target.key)
        }
        requireNotNull(container) {
            val targetName = when(instruction.target) {
                NavigationInstruction.ContainerOperation.Target.ParentContainer -> "ParentContainer"
                NavigationInstruction.ContainerOperation.Target.ActiveContainer -> "ActiveContainer"
                is NavigationInstruction.ContainerOperation.Target.TargetContainer -> "TargetContainer(${instruction.target.key})"
            }
            val contextKeyName = navigationContext.instruction.navigationKey::class.java.simpleName
            "Failed to perform container instruction for $targetName in context with key $contextKeyName: Could not find valid container to perform instruction on"
        }
        instruction.operation.invoke(container)
    }
}