package dev.enro.handle

import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.context.RootContext

internal actual fun <T : NavigationKey> RootNavigationHandle<T>.handleNavigationOperationForPlatform(
    operation: NavigationOperation,
    context: RootContext,
): Boolean {
    // The browser has no equivalent of opening another root context (a new tab
    // can't be reliably opened from app code, and closing the root would close
    // the tab itself), so there's no platform-specific handling to do here —
    // every operation is processed through normal container flow.
    return false
}
