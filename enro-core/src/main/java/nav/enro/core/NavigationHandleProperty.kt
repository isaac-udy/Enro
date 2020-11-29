package nav.enro.core

import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import nav.enro.core.context.ChildContainer
import nav.enro.core.internal.handle.NavigationHandleViewModel
import java.lang.ref.WeakReference
import kotlin.collections.set
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


class NavigationHandleProperty<Key : NavigationKey> @PublishedApi internal constructor(
    private val lifecycleOwner: LifecycleOwner,
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val configBuilder: NavigationHandleConfiguration<Key>.() -> Unit = {}
) : ReadOnlyProperty<Any, TypedNavigationHandle<Key>> {

    private val config = NavigationHandleConfiguration<Key>().apply(configBuilder)

    private val navigationHandle: TypedNavigationHandle<Key> by lazy {
        val navigationHandle = ViewModelProvider(viewModelStoreOwner, ViewModelProvider.NewInstanceFactory())
            .get(NavigationHandleViewModel::class.java)

        config.applyTo(navigationHandle)

        return@lazy navigationHandle.asTyped<Key>()
    }

    init {
        pendingProperties[lifecycleOwner.hashCode()] = WeakReference(this)
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): TypedNavigationHandle<Key> {
        return navigationHandle
    }

    companion object {
        internal val pendingProperties = mutableMapOf<Int, WeakReference<NavigationHandleProperty<*>>>()

        fun applyPending(activity: FragmentActivity) {
            val pending = pendingProperties[activity.hashCode()] ?: return
            pending.get()?.navigationHandle.hashCode()
            pendingProperties.remove(activity.hashCode())
        }

        fun applyPending(fragment: Fragment) {
            val pending = pendingProperties[fragment.hashCode()] ?: return
            pending.get()?.navigationHandle.hashCode()
            pendingProperties.remove(fragment.hashCode())
        }
    }
}

class NavigationHandleConfiguration<T : NavigationKey> @PublishedApi internal constructor() {

    private var childContainers: List<ChildContainer> = listOf()
    private var defaultKey: T? = null
    private var onCloseRequested: TypedNavigationHandle<T>.() -> Unit = { close() }

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
        navigationHandleViewModel.defaultKey = defaultKey
        navigationHandleViewModel.internalOnCloseRequested = { onCloseRequested(navigationHandleViewModel.asTyped()) }
    }
}


fun <T : NavigationKey> FragmentActivity.navigationHandle(
    config: NavigationHandleConfiguration<T>.() -> Unit = {}
): NavigationHandleProperty<T> = NavigationHandleProperty(
    lifecycleOwner = this,
    viewModelStoreOwner = this,
    configBuilder = config
)

fun <T : NavigationKey> Fragment.navigationHandle(
    config: NavigationHandleConfiguration<T>.() -> Unit = {}
): NavigationHandleProperty<T> = NavigationHandleProperty(
    lifecycleOwner = this,
    viewModelStoreOwner = this,
    configBuilder = config
)

fun FragmentActivity.getNavigationHandle(): NavigationHandle =
    viewModels<NavigationHandleViewModel> { ViewModelProvider.NewInstanceFactory() } .value

fun Fragment.getNavigationHandle(): NavigationHandle =
    viewModels<NavigationHandleViewModel> { ViewModelProvider.NewInstanceFactory() } .value