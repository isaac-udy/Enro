package dev.enro.core.internal.handle

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationContext
import dev.enro.core.NavigationInstruction
import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.usecase.ExecuteCloseInstruction
import dev.enro.core.controller.usecase.ExecuteContainerOperationInstruction
import dev.enro.core.controller.usecase.ExecuteOpenInstruction

/**
 * A special type of [NavigationHandleViewModel] for testing. This class  prevents
 * navigation instructions from being executed as they normally would be, instead
 * recording the instructions for verification during testing.
 *
 * When using the EnroTestRule, runEnroTest, or EnroTest.installNavigationController,
 * all NavigationHandles created will be instances of [TestNavigationHandleViewModel].
 */
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
    executeContainerOperationInstruction = object : ExecuteContainerOperationInstruction {
        override fun invoke(
            navigationContext: NavigationContext<out Any>,
            instruction: NavigationInstruction.ContainerOperation
        ) {}
    },
) {
    internal val instructions = mutableListOf<NavigationInstruction>()

    override fun executeInstruction(navigationInstruction: NavigationInstruction) {
        instructions.add(navigationInstruction)
    }
}