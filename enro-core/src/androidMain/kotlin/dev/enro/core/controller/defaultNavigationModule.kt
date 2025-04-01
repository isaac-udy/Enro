package dev.enro.core.controller

import dev.enro.core.activity.ActivityResultBridge
import dev.enro.core.activity.ActivityResultDestination
import dev.enro.core.compose.composableDestination
import dev.enro.core.controller.interceptor.HiltInstructionInterceptor
import dev.enro.core.controller.interceptor.InstructionOpenedByInterceptor
import dev.enro.core.controller.interceptor.NavigationContainerDelegateInterceptor
import dev.enro.core.hosts.hostNavigationModule
import dev.enro.core.internal.NoKeyNavigationBinding
import dev.enro.core.result.ForwardingResultInterceptor
import dev.enro.core.result.flows.NavigationFlowInterceptor
import dev.enro.destination.activity.ActivityPlugin
import dev.enro.destination.fragment.FragmentPlugin
import dev.enro.destination.synthetic.SyntheticExecutionInterceptor

internal val defaultNavigationModule = createNavigationModule {
    plugin(ActivityPlugin)
    plugin(FragmentPlugin)

    interceptor(SyntheticExecutionInterceptor)
    interceptor(NavigationContainerDelegateInterceptor)
    interceptor(InstructionOpenedByInterceptor)
    interceptor(HiltInstructionInterceptor)
    interceptor(NavigationFlowInterceptor)
    interceptor(ForwardingResultInterceptor)

    binding(NoKeyNavigationBinding())
    composableDestination<ActivityResultDestination> { ActivityResultBridge() }

    module(hostNavigationModule)
}