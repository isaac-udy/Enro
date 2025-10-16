package dev.enro.ui.destinations

import dev.enro.EnroController
import dev.enro.NavigationKey
import dev.enro.ui.NavigationDestination

// this object contains helper functions to decide if a destination is
// a destination that should open a root context or not
internal object RootContextDestination {
    internal const val IsRootContextDestinationKey = "dev.enro.ui.NavigationDestination.RootContextDestination.IsRootContextDestinationKey"
}

internal fun NavigationDestination.MetadataBuilder<*>.rootContextDestination() {
    add(RootContextDestination.IsRootContextDestinationKey to true)
}

internal fun NavigationKey.Instance<*>.isRootContextDestination(
    controller: EnroController,
): Boolean {
    return controller.bindings
        .bindingFor(this)
        .provider
        .peekMetadata(this)
        .get(RootContextDestination.IsRootContextDestinationKey) == true
}
