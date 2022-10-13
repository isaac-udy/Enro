package dev.enro.core.controller

import dev.enro.core.controller.interceptor.ExecutorContextInterceptor
import dev.enro.core.controller.interceptor.HiltInstructionInterceptor
import dev.enro.core.hosts.hostComponent
import dev.enro.core.internal.NoKeyNavigator
import dev.enro.core.result.EnroResult

internal val defaultComponent = createNavigationComponent {
    plugin(EnroResult())

    interceptor(ExecutorContextInterceptor())
    interceptor(HiltInstructionInterceptor())

    navigator(NoKeyNavigator())

    component(hostComponent)
}