package dev.enro.core.controller

import dev.enro.core.ArchitectureException
import dev.enro.core.controller.interceptors.HiltInstructionInterceptor
import dev.enro.core.controller.interceptors.NavigationInstructionContextInterceptor
import dev.enro.core.controller.interceptors.NavigationContainerDelegateInterceptor
import dev.enro.core.hosts.hostComponent
import dev.enro.core.internal.NoKeyNavigationBinding

@ArchitectureException("This pulls together implementation dependencies and should be ignored")
internal object DefaultComponent {
    val component = createNavigationComponent {
        interceptor(NavigationContainerDelegateInterceptor)
        interceptor(NavigationInstructionContextInterceptor)
        interceptor(HiltInstructionInterceptor)

        binding(NoKeyNavigationBinding())

        component(hostComponent)
    }
}