package dev.enro.core.controller

import dev.enro.core.controller.interceptor.HiltInstructionInterceptor
import dev.enro.core.controller.interceptor.InstructionOpenedByInterceptor
import dev.enro.core.controller.interceptor.NavigationContainerDelegateInterceptor
import dev.enro.core.hosts.hostNavigationModule
import dev.enro.core.internal.NoKeyNavigationBinding

internal val defaultNavigationModule = createNavigationModule {
    interceptor(NavigationContainerDelegateInterceptor)
    interceptor(InstructionOpenedByInterceptor)
    interceptor(HiltInstructionInterceptor)

    binding(NoKeyNavigationBinding())

    module(hostNavigationModule)
}