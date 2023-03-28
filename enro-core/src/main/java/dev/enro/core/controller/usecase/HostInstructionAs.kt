package dev.enro.core.controller.usecase

import dev.enro.core.NavigationContext
import dev.enro.core.NavigationInstruction
import dev.enro.core.controller.repository.NavigationBindingRepository
import dev.enro.core.controller.repository.NavigationHostFactoryRepository

internal class HostInstructionAs(
    private val navigationHostFactoryRepository: NavigationHostFactoryRepository,
    private val navigationBindingRepository: NavigationBindingRepository,
) {
    operator fun <HostType: Any> invoke(
        hostType: Class<HostType>,
        navigationContext: NavigationContext<*>,
        instruction: NavigationInstruction.Open<*>
    ): NavigationInstruction.Open<*> {
        val binding = navigationBindingRepository.bindingForKeyType(instruction.navigationKey::class)
            ?: throw IllegalStateException()
        val wrappedType = binding.baseType.java
        if (hostType == wrappedType) return instruction

        val host = navigationHostFactoryRepository.getNavigationHost(hostType, navigationContext, instruction)
            ?: throw IllegalStateException()

        val wrapped = host.wrap(navigationContext, instruction)
        if (wrapped == instruction) return instruction
        return wrapped.internal.copy(
            openingType = hostType,
            resultId = null
        )
    }

    inline operator fun <reified HostType: Any> invoke(
        navigationContext: NavigationContext<*>,
        instruction: NavigationInstruction.Open<*>,
    ): NavigationInstruction.Open<*> = invoke(
        hostType = HostType::class.java,
        navigationContext = navigationContext,
        instruction = instruction
    )
}


