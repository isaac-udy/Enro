package dev.enro3.controller

import dev.enro3.NavigationHandleConfiguration
import dev.enro3.ui.destinations.EmptyNavigationKey
import dev.enro3.ui.destinations.SyntheticDestination
import dev.enro3.ui.destinations.emptyDestination


internal val defaultNavigationModule = createNavigationModule {
    interceptor(SyntheticDestination.interceptor)
    interceptor(NavigationHandleConfiguration.onCloseCallbackInterceptor)
    destination<EmptyNavigationKey>(emptyDestination())
}
