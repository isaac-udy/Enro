package dev.enro.controller

import dev.enro.NavigationHandleConfiguration
import dev.enro.ui.destinations.EmptyNavigationKey
import dev.enro.ui.destinations.SyntheticDestination
import dev.enro.ui.destinations.emptyDestination

internal val defaultNavigationModule = createNavigationModule {
    interceptor(SyntheticDestination.interceptor)
    interceptor(NavigationHandleConfiguration.onCloseCallbackInterceptor)
    destination<EmptyNavigationKey>(emptyDestination())
}
