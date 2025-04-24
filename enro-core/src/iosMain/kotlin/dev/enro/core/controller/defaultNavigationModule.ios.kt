package dev.enro.core.controller

import dev.enro.core.controller.interceptor.InstructionOpenedByInterceptor
import dev.enro.core.controller.interceptor.NavigationContainerDelegateInterceptor
import dev.enro.core.internal.NoKeyNavigationBinding
import dev.enro.core.result.ForwardingResultInterceptor
import dev.enro.core.result.flows.NavigationFlowInterceptor
import dev.enro.destination.synthetic.SyntheticExecutionInterceptor

internal actual val defaultNavigationModule: NavigationModule = createNavigationModule {
    interceptor(SyntheticExecutionInterceptor)
    interceptor(NavigationContainerDelegateInterceptor)
    interceptor(InstructionOpenedByInterceptor)
    interceptor(NavigationFlowInterceptor)
    interceptor(ForwardingResultInterceptor)

    binding(NoKeyNavigationBinding())
}