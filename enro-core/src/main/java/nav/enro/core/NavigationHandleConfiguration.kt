package nav.enro.core

import androidx.annotation.IdRes
import nav.enro.core.internal.handle.NavigationHandleViewModel
import kotlin.reflect.KClass

internal class ChildContainer(
    @IdRes val containerId: Int,
    val accept: (NavigationKey) -> Boolean
)

// TODO Move this to being a "Builder" and add data class for configuration?
class NavigationHandleConfiguration<T : NavigationKey> @PublishedApi internal constructor(
    private val keyType: KClass<T>
) {
    internal var childContainers: List<ChildContainer> = listOf()
        private set

    internal var defaultKey: T? = null
        private set

    internal var onCloseRequested: TypedNavigationHandle<T>.() -> Unit = { close() }
        private set

    fun container(@IdRes containerId: Int, accept: (NavigationKey) -> Boolean = { true }) {
        childContainers = childContainers + ChildContainer(containerId, accept)
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