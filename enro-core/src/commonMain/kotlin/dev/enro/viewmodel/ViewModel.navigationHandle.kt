package dev.enro.viewmodel

import androidx.lifecycle.ViewModel
import dev.enro.core.NavigationHandle


private class ClosableNavigationHandleReference(
    navigationHandle: NavigationHandle,
) : AutoCloseable {
    var navigationHandle: NavigationHandle? = navigationHandle
    override fun close() {
        navigationHandle = null
    }

    companion object {
        const val NAVIGATION_HANDLE_KEY = "dev.enro.viemodel.NAVIGATION_HANDLE_KEY"
    }
}

internal fun ViewModel.setNavigationHandle(navigationHandle: NavigationHandle) {
    addCloseable(
        key = ClosableNavigationHandleReference.NAVIGATION_HANDLE_KEY,
        closeable = ClosableNavigationHandleReference(navigationHandle),
    )
}

internal val ViewModel.navigationHandle: NavigationHandle?
    get() {
        val closeableReference = getCloseable<ClosableNavigationHandleReference>(
            key = ClosableNavigationHandleReference.NAVIGATION_HANDLE_KEY
        ) ?: return null

        return closeableReference.navigationHandle
    }
