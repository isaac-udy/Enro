package dev.enro.core.controller

import dev.enro.animation.NavigationAnimationForComposable
import dev.enro.animation.NavigationAnimationForView
import dev.enro.core.activity.ActivityResultBridge
import dev.enro.core.activity.ActivityResultDestination
import dev.enro.core.controller.interceptor.HiltInstructionInterceptor
import dev.enro.core.controller.interceptor.InstructionOpenedByInterceptor
import dev.enro.core.controller.interceptor.NavigationContainerDelegateInterceptor
import dev.enro.core.hosts.hostNavigationModule
import dev.enro.core.internal.NoKeyNavigationBinding
import dev.enro.core.result.ForwardingResultInterceptor
import dev.enro.core.result.flows.NavigationFlowInterceptor
import dev.enro.destination.activity.ActivityPlugin
import dev.enro.destination.compose.composableDestination
import dev.enro.destination.fragment.FragmentPlugin
import dev.enro.destination.synthetic.SyntheticExecutionInterceptor

internal actual val defaultNavigationModule = createNavigationModule {
    plugin(ActivityPlugin)
    plugin(FragmentPlugin)

    animations {
        defaults(NavigationAnimationForComposable.Defaults)
        defaults(NavigationAnimationForView.Defaults)
    }

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