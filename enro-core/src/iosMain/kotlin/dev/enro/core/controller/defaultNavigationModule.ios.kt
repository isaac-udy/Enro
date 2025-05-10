package dev.enro.core.controller

import dev.enro.animation.NavigationAnimationForComposable
import dev.enro.core.controller.interceptor.InstructionOpenedByInterceptor
import dev.enro.core.controller.interceptor.NavigationContainerDelegateInterceptor
import dev.enro.core.internal.NoKeyNavigationBinding
import dev.enro.core.result.ForwardingResultInterceptor
import dev.enro.core.result.flows.NavigationFlowInterceptor
import dev.enro.destination.compose.composableDestination
import dev.enro.destination.ios.hosts.ComposableHostForUIViewController
import dev.enro.destination.ios.hosts.HostComposableInUIWindow
import dev.enro.destination.ios.hosts.HostUIViewControllerInCompose
import dev.enro.destination.ios.hosts.HostUIViewControllerInComposeScreen
import dev.enro.destination.ios.hosts.HostUIViewControllerInUIWindow
import dev.enro.destination.ios.hosts.UIWindowHost
import dev.enro.destination.ios.hosts.windowForHostingComposable
import dev.enro.destination.ios.hosts.windowForHostingUIViewController
import dev.enro.destination.ios.uiWindowDestination
import dev.enro.destination.synthetic.SyntheticExecutionInterceptor

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

    navigationHostFactory(ComposableHostForUIViewController())
    composableDestination<HostUIViewControllerInCompose> { HostUIViewControllerInComposeScreen() }

    navigationHostFactory(UIWindowHost())
    uiWindowDestination<HostUIViewControllerInUIWindow>(HostUIViewControllerInUIWindow.serializer(), windowForHostingUIViewController)
    uiWindowDestination<HostComposableInUIWindow>(HostComposableInUIWindow.serializer(), windowForHostingComposable)
}