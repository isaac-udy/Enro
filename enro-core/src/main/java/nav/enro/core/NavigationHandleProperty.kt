package nav.enro.core

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import nav.enro.core.context.ChildContainer
import nav.enro.core.internal.handle.NavigationHandleViewModel
import nav.enro.core.internal.handle.getNavigationHandleViewModel
import java.lang.ref.WeakReference
import kotlin.collections.set
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


class NavigationHandleProperty<Key : NavigationKey> @PublishedApi internal constructor(
    private val lifecycleOwner: LifecycleOwner,
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val configBuilder: NavigationHandleConfiguration<Key>.() -> Unit = {},
    private val keyType: KClass<Key>
) : ReadOnlyProperty<Any, TypedNavigationHandle<Key>> {

    private val config = NavigationHandleConfiguration(keyType).apply(configBuilder)

    private val navigationHandle: TypedNavigationHandle<Key> by lazy {
        val navigationHandle = viewModelStoreOwner.getNavigationHandleViewModel()
        return@lazy TypedNavigationHandleImpl<Key>(navigationHandle)
    }

    init {
        pendingProperties[lifecycleOwner.hashCode()] = WeakReference(this)
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): TypedNavigationHandle<Key> {
        return navigationHandle
    }

    companion object {
        internal val pendingProperties = mutableMapOf<Int, WeakReference<NavigationHandleProperty<*>>>()

        fun getPendingConfig(activity: FragmentActivity): NavigationHandleConfiguration<*>? {
            val pending = pendingProperties[activity.hashCode()] ?: return null
            val config = pending.get()?.config
            pendingProperties.remove(activity.hashCode())
            return config
        }

        fun getPendingConfig(fragment: Fragment): NavigationHandleConfiguration<*>? {
            val pending = pendingProperties[fragment.hashCode()] ?: return null
            val config = pending.get()?.config
            pendingProperties.remove(fragment.hashCode())
            return config
        }
    }
}

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

    internal fun applyTo(navigationHandleViewModel: NavigationHandleViewModel) {
        navigationHandleViewModel.childContainers = childContainers
        navigationHandleViewModel.internalOnCloseRequested = { onCloseRequested(navigationHandleViewModel.asTyped(keyType)) }
    }
}


inline fun <reified T: NavigationKey> FragmentActivity.navigationHandle(
    noinline config: NavigationHandleConfiguration<T>.() -> Unit = {}
): NavigationHandleProperty<T> = NavigationHandleProperty(
    lifecycleOwner = this,
    viewModelStoreOwner = this,
    configBuilder = config,
    keyType = T::class
)

inline fun <reified T : NavigationKey> Fragment.navigationHandle(
    noinline config: NavigationHandleConfiguration<T>.() -> Unit = {}
): NavigationHandleProperty<T> = NavigationHandleProperty(
    lifecycleOwner = this,
    viewModelStoreOwner = this,
    configBuilder = config,
    keyType = T::class
)

fun FragmentActivity.getNavigationHandle(): NavigationHandle = getNavigationHandleViewModel()

fun Fragment.getNavigationHandle(): NavigationHandle = getNavigationHandleViewModel()