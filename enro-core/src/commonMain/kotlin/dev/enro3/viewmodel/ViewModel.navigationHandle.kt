package dev.enro3.viewmodel

import androidx.lifecycle.ViewModel
import dev.enro3.NavigationHandle
import dev.enro3.NavigationKey


@PublishedApi
internal class ClosableNavigationHandleReference() : AutoCloseable {
    var navigationHandle: NavigationHandle<NavigationKey>? = null

    override fun close() {
        navigationHandle = null
    }

    companion object {
        const val NAVIGATION_HANDLE_KEY = "dev.enro.viemodel.NAVIGATION_HANDLE_KEY"
    }
}

@PublishedApi
internal val ViewModel.navigationHandleReference: ClosableNavigationHandleReference
    get() {
        val closeableReference = getCloseable(
            key = ClosableNavigationHandleReference.NAVIGATION_HANDLE_KEY
        ) ?: ClosableNavigationHandleReference().also { reference ->
            addCloseable(
                key = ClosableNavigationHandleReference.NAVIGATION_HANDLE_KEY,
                closeable = reference,
            )
        }
        return closeableReference
    }

