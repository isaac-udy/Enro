package dev.enro.core.controller

import dev.enro.core.controller.interceptor.HiltInstructionInterceptor
import dev.enro.core.controller.interceptor.InstructionOpenedByInterceptor
import dev.enro.core.hosts.hostComponent
import dev.enro.core.internal.NoKeyNavigationBinding
import dev.enro.core.result.EnroResult

internal val defaultComponent = createNavigationComponent {
    plugin(EnroResult())

    interceptor(InstructionOpenedByInterceptor)
    interceptor(HiltInstructionInterceptor)

    binding(NoKeyNavigationBinding())

    component(hostComponent)
}