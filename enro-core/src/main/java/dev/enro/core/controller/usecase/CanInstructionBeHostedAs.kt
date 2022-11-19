package dev.enro.core.controller.usecase

import dev.enro.core.NavigationContext
import dev.enro.core.NavigationInstruction
import dev.enro.core.controller.repository.NavigationBindingRepository
import dev.enro.core.controller.repository.NavigationHostFactoryRepository

internal class CanInstructionBeHostedAs(
    private val navigationHostFactoryRepository: NavigationHostFactoryRepository,
    private val navigationBindingRepository: NavigationBindingRepository,
) {
    operator fun <HostType: Any> invoke(
        hostType: Class<HostType>,
        navigationContext: NavigationContext<*>,
        instruction: NavigationInstruction.Open<*>
    ): Boolean {
        val binding = navigationBindingRepository.bindingForKeyType(instruction.navigationKey::class) ?: return false
        val wrappedType = binding.baseType.java
        if (hostType == wrappedType) return true

        val host = navigationHostFactoryRepository.getNavigationHost(hostType, navigationContext, instruction)
        return host != null
    }
}