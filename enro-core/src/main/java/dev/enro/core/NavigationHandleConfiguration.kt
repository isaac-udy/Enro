package dev.enro.core

import androidx.annotation.IdRes
import dev.enro.core.compose.AbstractComposeFragmentHostKey
import dev.enro.core.internal.handle.NavigationHandleViewModel
import kotlin.reflect.KClass

// TODO Move this to being a "Builder" and add data class for configuration?
class NavigationHandleConfiguration<T : NavigationKey> @PublishedApi internal constructor(
    private val keyType: KClass<T>
) {
    internal var childContainers: List<NavigationContainer> = listOf()
        private set

    internal var defaultKey: T? = null
        private set

    internal var onCloseRequested: TypedNavigationHandle<T>.() -> Unit = { close() }
        private set

    @Deprecated("TODO") // TODO
    fun container(@IdRes containerId: Int, accept: (NavigationKey) -> Boolean = { true }) {
        childContainers = childContainers + NavigationContainer(
            containerId = containerId,
            accept = accept
        )
    }

    fun defaultKey(navigationKey: T) {
        defaultKey = navigationKey
    }

    fun onCloseRequested(block: TypedNavigationHandle<T>.() -> Unit) {
        onCloseRequested = block
    }

    // TODO Store these properties ON the navigation handle? Rather than set individual fields?
    internal fun applyTo(navigationHandleViewModel: NavigationHandleViewModel) {
        navigationHandleViewModel.childContainers = childContainers
        navigationHandleViewModel.internalOnCloseRequested = { onCloseRequested(navigationHandleViewModel.asTyped(keyType)) }
    }
}

class LazyNavigationHandleConfiguration<T: NavigationKey>(
    private val keyType: KClass<T>
) {

    private var onCloseRequested: TypedNavigationHandle<T>.() -> Unit = { close() }

    fun onCloseRequested(block: TypedNavigationHandle<T>.() -> Unit) {
        onCloseRequested = block
    }

    fun configure(navigationHandle: NavigationHandle) {
        val handle = if(navigationHandle is TypedNavigationHandleImpl<*>) {
            navigationHandle.navigationHandle
        } else navigationHandle

        if(handle is NavigationHandleViewModel) {
            handle.internalOnCloseRequested = { onCloseRequested(navigationHandle.asTyped(keyType)) }
        }
    }
}