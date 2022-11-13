package dev.enro.core

import androidx.annotation.IdRes
import dev.enro.core.usecase.ConfigureNavigationHandle
import kotlin.reflect.KClass

internal class ChildContainer(
    @IdRes val containerId: Int,
    private val accept: (NavigationKey) -> Boolean
) {
    fun accept(key: NavigationKey): Boolean {
        return accept.invoke(key)
    }
}

internal data class NavigationHandleConfigurationProperties<T : NavigationKey>(
    val keyType: KClass<T>,
    val childContainers: List<ChildContainer> = emptyList(),
    val defaultKey: T? = null,
    val onCloseRequested: (TypedNavigationHandle<T>.() -> Unit)? = null
)

// TODO Move this to being a "Builder" and add data class for configuration?
public class NavigationHandleConfiguration<T : NavigationKey> @PublishedApi internal constructor(
    keyType: KClass<T>
) {
    internal var properties = NavigationHandleConfigurationProperties(keyType)

    @Deprecated("Please use the `by navigationContainer` extensions in FragmentActivity and Fragment to create containers")
    public fun container(@IdRes containerId: Int, accept: (NavigationKey) -> Boolean = { true }) {
        properties = properties.copy(
            childContainers = properties.childContainers + ChildContainer(containerId, accept)
        )
    }

    public fun defaultKey(navigationKey: T) {
        properties = properties.copy(
            defaultKey = navigationKey
        )
    }

    public fun onCloseRequested(block: TypedNavigationHandle<T>.() -> Unit) {
        properties = properties.copy(
            onCloseRequested = block
        )
    }

    // TODO Store these properties ON the navigation handle? Rather than set individual fields?
    internal fun configure(navigationHandle: NavigationHandle) {
        navigationHandle.dependencyScope.get<ConfigureNavigationHandle>()
            .invoke(
                configuration = properties,
                navigationHandle = navigationHandle
            )
    }
}

public class LazyNavigationHandleConfiguration<T : NavigationKey>(
    keyType: KClass<T>
) {

    private var properties = NavigationHandleConfigurationProperties(keyType)

    public fun onCloseRequested(block: TypedNavigationHandle<T>.() -> Unit) {
        properties = properties.copy(
            onCloseRequested = block
        )
    }

    public fun configure(
        navigationHandle: NavigationHandle,
    ) {
        navigationHandle.dependencyScope.get<ConfigureNavigationHandle>()
            .invoke(
                configuration = properties,
                navigationHandle = navigationHandle
            )
    }
}