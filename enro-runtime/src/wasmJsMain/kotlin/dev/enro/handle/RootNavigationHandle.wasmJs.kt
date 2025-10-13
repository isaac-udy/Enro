package dev.enro.handle

import dev.enro.NavigationKey
import dev.enro.NavigationOperation
import dev.enro.context.RootContext

internal actual fun <T : NavigationKey> RootNavigationHandle<T>.handleNavigationOperationForPlatform(
    operation: NavigationOperation,
    context: RootContext,
): Boolean {
    TODO("Not yet implemented")
}