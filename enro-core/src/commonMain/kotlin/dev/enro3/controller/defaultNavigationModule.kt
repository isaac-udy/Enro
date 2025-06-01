package dev.enro3.controller

import dev.enro3.ui.destinations.SyntheticDestination


internal val defaultNavigationModule = createNavigationModule {
    interceptor(SyntheticDestination.interceptor)
}
