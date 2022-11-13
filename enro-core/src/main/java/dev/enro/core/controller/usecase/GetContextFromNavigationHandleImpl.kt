package dev.enro.core.controller.usecase

import dev.enro.core.NavigationContext
import dev.enro.core.NavigationHandle
import dev.enro.core.TypedNavigationHandleImpl
import dev.enro.core.internal.handle.NavigationHandleViewModel
import dev.enro.core.usecase.GetContextFromNavigationHandle

internal class GetContextFromNavigationHandleImpl : GetContextFromNavigationHandle {
    override fun invoke(navigationHandle: NavigationHandle): NavigationContext<*>? {
        val unwrapped = when(navigationHandle) {
            is TypedNavigationHandleImpl<*> -> navigationHandle.navigationHandle
            else -> navigationHandle
        }

        return when(unwrapped) {
            is NavigationHandleViewModel -> unwrapped.navigationContext
            else -> null
        }
    }
}