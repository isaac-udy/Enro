package dev.enro.core.controller

import dev.enro.core.controller.interceptor.HiltInstructionInterceptor
import dev.enro.core.controller.interceptor.InstructionOpenedByInterceptor
import dev.enro.core.controller.interceptor.NavigationContainerDelegateInterceptor
import dev.enro.core.hosts.hostComponent
import dev.enro.core.internal.NoKeyNavigationBinding

internal val defaultComponent = createNavigationComponent {
    interceptor(NavigationContainerDelegateInterceptor)
    interceptor(InstructionOpenedByInterceptor)
    interceptor(HiltInstructionInterceptor)

    binding(NoKeyNavigationBinding())

    component(hostComponent)
}