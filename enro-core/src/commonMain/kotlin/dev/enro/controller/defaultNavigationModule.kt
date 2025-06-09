package dev.enro.controller

import dev.enro.NavigationHandleConfiguration
import dev.enro.context.PreviouslyActiveContainerInterceptor
import dev.enro.ui.destinations.EmptyNavigationKey
import dev.enro.ui.destinations.SyntheticDestination
import dev.enro.ui.destinations.emptyDestination

internal val defaultNavigationModule = createNavigationModule {
    interceptor(SyntheticDestination.interceptor)
    interceptor(PreviouslyActiveContainerInterceptor)
    interceptor(NavigationHandleConfiguration.onCloseCallbackInterceptor)
    destination<EmptyNavigationKey>(emptyDestination())
}
