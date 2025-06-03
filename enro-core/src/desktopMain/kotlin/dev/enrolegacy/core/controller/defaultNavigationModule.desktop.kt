package dev.enrolegacy.core.controller

import dev.enro.animation.NavigationAnimationForComposable
import dev.enro.core.controller.interceptor.InstructionOpenedByInterceptor
import dev.enro.core.controller.interceptor.NavigationContainerDelegateInterceptor
import dev.enro.core.internal.NoKeyNavigationBinding
import dev.enro.core.result.ForwardingResultInterceptor
import dev.enro.core.result.flows.NavigationFlowInterceptor
import dev.enro.destination.desktop.DesktopWindowHostForComposable
import dev.enro.destination.desktop.OpenComposableInDesktopWindow
import dev.enro.destination.desktop.desktopWindowDestination
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

    desktopWindowDestination<OpenComposableInDesktopWindow, DesktopWindowHostForComposable> { DesktopWindowHostForComposable() }

    binding(NoKeyNavigationBinding())
}