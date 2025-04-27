package dev.enro.core.controller.usecase

import dev.enro.core.NavigationContext
import dev.enro.core.NavigationInstruction
import dev.enro.core.controller.repository.NavigationBindingRepository
import dev.enro.core.controller.repository.NavigationHostFactoryRepository
import kotlin.reflect.KClass

internal class CanInstructionBeHostedAs(
    private val navigationHostFactoryRepository: NavigationHostFactoryRepository,
    private val navigationBindingRepository: NavigationBindingRepository,
) {
    operator fun <HostType: Any> invoke(
        hostType: KClass<HostType>,
        navigationContext: NavigationContext<*>,
        instruction: NavigationInstruction.Open<*>
    ): Boolean {
        val binding = navigationBindingRepository.bindingForInstruction(instruction) ?: return false
        val wrappedType = binding.baseType
        if (hostType == wrappedType) return true

        val host = navigationHostFactoryRepository.getNavigationHost(hostType, navigationContext, instruction)
        return host != null
    }
}