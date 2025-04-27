package dev.enro.core.controller.usecase

import dev.enro.core.AnyOpenInstruction
import dev.enro.core.NavigationBinding
import dev.enro.core.controller.repository.NavigationBindingRepository

internal class GetNavigationBinding(
    private val navigationBindingRepository: NavigationBindingRepository,
) {
    operator fun invoke(instruction: AnyOpenInstruction): NavigationBinding<*, *>? {
        return navigationBindingRepository.bindingForInstruction(instruction)
    }

    fun require(instruction: AnyOpenInstruction): NavigationBinding<*, *> {
        return requireNotNull(invoke(instruction))
    }
}