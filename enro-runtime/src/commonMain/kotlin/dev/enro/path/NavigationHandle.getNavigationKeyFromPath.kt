package dev.enro.path

import dev.enro.EnroController
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.annotations.ExperimentalEnroApi

@ExperimentalEnroApi
public fun NavigationHandle<*>.getNavigationKeyFromPath(
    path: String,
): NavigationKey? {
    return EnroController.requireInstance().getNavigationKeyFromPath(path)
}
