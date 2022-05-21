package dev.enro.core

import dev.enro.core.internal.handle.NavigationHandleViewModel
import kotlin.reflect.KClass

// TODO Move this to being a "Builder" and add data class for configuration?
class NavigationHandleConfiguration<T : NavigationKey> @PublishedApi internal constructor(
    private val keyType: KClass<T>
) {
    internal var defaultKey: T? = null
        private set

    internal var onCloseRequested: (TypedNavigationHandle<T>.() -> Unit)? = null
        private set

    fun defaultKey(navigationKey: T) {
        defaultKey = navigationKey
    }

    fun onCloseRequested(block: TypedNavigationHandle<T>.() -> Unit) {
        onCloseRequested = block
    }

    // TODO Store these properties ON the navigation handle? Rather than set individual fields?
    internal fun applyTo(navigationHandleViewModel: NavigationHandleViewModel) {
        val onCloseRequested = onCloseRequested ?: return
        navigationHandleViewModel.internalOnCloseRequested = { onCloseRequested(navigationHandleViewModel.asTyped(keyType)) }
    }
}

class LazyNavigationHandleConfiguration<T : NavigationKey>(
    private val keyType: KClass<T>
) {

    private var onCloseRequested: (TypedNavigationHandle<T>.() -> Unit)? = null

    fun onCloseRequested(block: TypedNavigationHandle<T>.() -> Unit) {
        onCloseRequested = block
    }

    fun configure(navigationHandle: NavigationHandle) {
        val handle = if (navigationHandle is TypedNavigationHandleImpl<*>) {
            navigationHandle.navigationHandle
        } else navigationHandle

        val onCloseRequested = onCloseRequested ?: return

        if (handle is NavigationHandleViewModel) {
            handle.internalOnCloseRequested = { onCloseRequested(navigationHandle.asTyped(keyType)) }
        } else if (handle.controller.isInTest) {
            val field = handle::class.java.declaredFields
                .firstOrNull { it.name.startsWith("internalOnCloseRequested") }
                ?: return
            field.isAccessible = true
            field.set(handle, { onCloseRequested(navigationHandle.asTyped(keyType)) })
        }
    }
}