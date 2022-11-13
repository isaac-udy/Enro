package dev.enro.core.usecase

import dev.enro.core.NavigationContext
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationHandleConfigurationProperties
import dev.enro.core.NavigationKey

internal interface ConfigureNavigationHandle {
    operator fun <T: NavigationKey> invoke(
        configuration: NavigationHandleConfigurationProperties<T>,
        navigationHandle: NavigationHandle
    )
}