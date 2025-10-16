package dev.enro.controller

import dev.enro.NavigationHandleConfiguration
import dev.enro.controller.interceptors.PreviouslyActiveContainerInterceptor
import dev.enro.controller.interceptors.RootDestinationInterceptor
import dev.enro.ui.destinations.EmptyNavigationKey
import dev.enro.ui.destinations.SyntheticDestination
import dev.enro.ui.destinations.emptyDestination

internal val defaultNavigationModule = createNavigationModule {
    interceptor(RootDestinationInterceptor)
    interceptor(SyntheticDestination.interceptor)
    interceptor(PreviouslyActiveContainerInterceptor)
    destination<EmptyNavigationKey>(emptyDestination())
}
