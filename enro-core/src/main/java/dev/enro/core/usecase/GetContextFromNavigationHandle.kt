package dev.enro.core.usecase

import dev.enro.core.NavigationContext
import dev.enro.core.NavigationHandle
import dev.enro.core.get

internal fun NavigationHandle.getNavigationContext(): NavigationContext<*>? {
    return dependencyScope.get<GetContextFromNavigationHandle>().invoke(this)
}

internal fun NavigationHandle.requireNavigationContext(): NavigationContext<*> {
    return requireNotNull(dependencyScope.get<GetContextFromNavigationHandle>().invoke(this))
}

internal interface GetContextFromNavigationHandle {
    operator fun invoke(navigationHandle: NavigationHandle): NavigationContext<*>?
}