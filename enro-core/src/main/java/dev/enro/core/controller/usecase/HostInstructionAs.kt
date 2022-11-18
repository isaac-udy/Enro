package dev.enro.core.controller.usecase

import dev.enro.core.NavigationInstruction
import dev.enro.core.controller.repository.NavigationBindingRepository
import dev.enro.core.controller.repository.NavigationHostFactoryRepository

internal class HostInstructionAs(
    private val navigationHostFactoryRepository: NavigationHostFactoryRepository,
    private val navigationBindingRepository: NavigationBindingRepository,
) {
    operator fun <HostType: Any> invoke(hostType: Class<HostType>, instruction: NavigationInstruction.Open<*>): NavigationInstruction.Open<*> {
        val binding = navigationBindingRepository.bindingForKeyType(instruction.navigationKey::class)
            ?: throw IllegalStateException()
        val wrappedType = binding.baseType.java
        if (hostType == wrappedType) return instruction

        val host = navigationHostFactoryRepository.getNavigationHost(hostType, instruction)
            ?: throw IllegalStateException()

        return host.wrap(instruction).internal.copy(
            openingType = hostType
        )
    }

    inline operator fun <reified HostType: Any> invoke(
        instruction: NavigationInstruction.Open<*>
    ): NavigationInstruction.Open<*> = invoke(HostType::class.java, instruction)
}


