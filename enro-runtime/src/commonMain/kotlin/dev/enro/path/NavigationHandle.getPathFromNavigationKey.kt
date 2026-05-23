package dev.enro.path

import dev.enro.EnroController
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.annotations.ExperimentalEnroApi

@ExperimentalEnroApi
public fun NavigationHandle<*>.getPathFromNavigationKey(
    key: NavigationKey,
): String? {
    return EnroController.requireInstance().getPathFromNavigationKey(key)
}
