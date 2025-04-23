package dev.enro.core

import dev.enro.core.controller.NavigationController
import dev.enro.core.controller.get
import dev.enro.core.controller.usecase.extras
import dev.enro.core.internal.handle.NavigationHandleViewModel
import kotlin.reflect.KClass

public class NavigationHandleConfiguration<T : NavigationKey> @PublishedApi internal constructor(
    private val keyType: KClass<T>
) {
    internal var defaultKey: T? = null
        private set

    internal var onCloseRequested: (TypedNavigationHandle<T>.() -> Unit)? = null
        private set

    public fun defaultKey(navigationKey: T) {
        defaultKey = navigationKey
    }

    public fun onCloseRequested(block: TypedNavigationHandle<T>.() -> Unit) {
        onCloseRequested = block
    }

    internal fun applyTo(context: NavigationContext<*>, navigationHandleViewModel: NavigationHandleViewModel) {
        val onCloseRequested = onCloseRequested ?: return
        navigationHandleViewModel.internalOnCloseRequested = { onCloseRequested(navigationHandleViewModel.asTyped(keyType)) }
    }
}

public class LazyNavigationHandleConfiguration<T : NavigationKey>(
    private val keyType: KClass<T>
) {

    private var onCloseRequested: (TypedNavigationHandle<T>.() -> Unit)? = null

    public fun onCloseRequested(block: TypedNavigationHandle<T>.() -> Unit) {
        onCloseRequested = block
    }

    public fun configure(navigationHandle: NavigationHandle) {
        val handle = if (navigationHandle is TypedNavigationHandleImpl<*>) {
            navigationHandle.navigationHandle
        } else navigationHandle

        val onCloseRequested = onCloseRequested ?: return

        if (handle is NavigationHandleViewModel) {
            handle.internalOnCloseRequested =
                { onCloseRequested(navigationHandle.asTyped(keyType)) }
        } else if (handle.dependencyScope.get<NavigationController>().config.isInTest) {
            handle.extras["TestNavigationHandle.internalOnCloseRequested"] = {
                onCloseRequested(navigationHandle.asTyped(keyType))
            }
        }
    }
}