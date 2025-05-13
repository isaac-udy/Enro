package dev.enro.core.controller.usecase

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.EnroException
import dev.enro.core.NavigationContext
import dev.enro.core.container.findContainerFor
import dev.enro.core.container.setBackstack
import dev.enro.core.controller.repository.InstructionInterceptorRepository
import dev.enro.core.controller.repository.NavigationBindingRepository
import dev.enro.core.getNavigationHandle
import dev.enro.core.window.isOpenInWindow

internal interface ExecuteOpenInstruction {
    operator fun invoke(
        navigationContext: NavigationContext<out Any>,
        instruction: AnyOpenInstruction
    )
}

internal class ExecuteOpenInstructionImpl(
    private val bindingRepository: NavigationBindingRepository,
    private val interceptorRepository: InstructionInterceptorRepository
) : ExecuteOpenInstruction {
    override operator fun invoke(
        navigationContext: NavigationContext<out Any>,
        instruction: AnyOpenInstruction
    ) {
        val binding = bindingRepository.bindingForInstruction(instruction)
            ?: throw EnroException.MissingNavigationBinding(instruction.navigationKey)

        val processedInstruction = interceptorRepository.intercept(
            instruction, navigationContext, binding
        ) ?: return

        if (processedInstruction.navigationKey::class != binding.keyType) {
            navigationContext.getNavigationHandle().executeInstruction(processedInstruction)
            return
        }

        val openInWindow = instruction.isOpenInWindow()
        if (openInWindow) {
            val controller = navigationContext.controller
            controller.windowManager.open(processedInstruction)
            return
        }
        val container = findContainerFor(navigationContext, processedInstruction)
        val isStrictMode = navigationContext.controller.config.isStrictMode
        when  {
            container != null -> container.setBackstack { backstack ->
                backstack.plus(processedInstruction)
            }
            isStrictMode -> {
                error("No container was found for NavigationInstruction.Open with NavigationKey " +
                        "${processedInstruction.navigationKey}. This error can be disabled by " +
                        "setting the strictMode flag to false when creating a NavigationController, " +
                        "but this is not recommended for most applications (it is better to figure out " +
                        "why there is no container for the instruction, or explicitly set the instruction " +
                        "to open in a new window by using the openInWindow extensions on NavigationKey or " +
                        "NavigationInstruction."
                )
            }
            else -> {
                val controller = navigationContext.controller
                controller.windowManager.open(processedInstruction)
            }
        }
    }
}