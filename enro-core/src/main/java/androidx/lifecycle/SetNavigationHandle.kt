package androidx.lifecycle

import dev.enro.core.NavigationHandle


internal const val NAVIGATION_HANDLE_KEY = "dev.enro.viemodel.NAVIGATION_HANDLE_KEY"

internal fun ViewModel.setNavigationHandleTag(navigationHandle: NavigationHandle) {
    setTagIfAbsent(
        NAVIGATION_HANDLE_KEY,
        navigationHandle
    )
}

internal fun ViewModel.getNavigationHandleTag(): NavigationHandle? {
    return getTag<NavigationHandle>(
        NAVIGATION_HANDLE_KEY
    )
}