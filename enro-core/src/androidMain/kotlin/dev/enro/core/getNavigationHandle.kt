package dev.enro.core

import dev.enro.core.internal.handle.getNavigationHandleViewModel
import dev.enro.destination.compose.ComposableDestination

/**
 * Android-specific implementation to get NavigationHandle from ComposableDestination
 */
internal fun ComposableDestination.getNavigationHandleForComposable(): NavigationHandle {
    return owner.getNavigationHandleViewModel()
}