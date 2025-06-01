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

// TODO do we actually want this to be done with a property?
public inline fun <reified K : NavigationKey> ViewModel.navigationHandle(): NavigationHandle<K> {
    val reference = navigationHandleReference
    if (reference.navigationHandle == null) {
        reference.navigationHandle = NavigationHandleProvider.get(this::class)
    }
    val navigationHandle = reference.navigationHandle
    requireNotNull(navigationHandle) {
        "Unable to retrieve navigation handle for ViewModel ${this::class.simpleName}"
    }
    require(navigationHandle.key is K) {
        "The navigation handle key does not match the expected type. Expected ${K::class.simpleName}, but got ${navigationHandle.key::class.simpleName}"
    }
    @Suppress("UNCHECKED_CAST")
    return navigationHandle as NavigationHandle<K>
}
