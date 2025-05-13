package dev.enro.core.controller

import dev.enro.animation.NavigationAnimationForComposable
import dev.enro.core.controller.interceptor.InstructionOpenedByInterceptor
import dev.enro.core.controller.interceptor.NavigationContainerDelegateInterceptor
import dev.enro.core.internal.NoKeyNavigationBinding
import dev.enro.core.result.ForwardingResultInterceptor
import dev.enro.core.result.flows.NavigationFlowInterceptor
import dev.enro.destination.synthetic.SyntheticExecutionInterceptor
import dev.enro.destination.web.HostComposableInWebWindow
import dev.enro.destination.web.WindowForHostingComposable
import dev.enro.destination.web.webWindowDestination

internal actual val defaultNavigationModule: NavigationModule = createNavigationModule {
    animations {
        defaults(NavigationAnimationForComposable.Defaults)
    }
    interceptor(SyntheticExecutionInterceptor)
    interceptor(NavigationContainerDelegateInterceptor)
    interceptor(InstructionOpenedByInterceptor)
    interceptor(NavigationFlowInterceptor)
    interceptor(ForwardingResultInterceptor)

    binding(NoKeyNavigationBinding())
    webWindowDestination<HostComposableInWebWindow, WindowForHostingComposable> { WindowForHostingComposable() }
}