package nav.enro.core

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
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

        fun getPendingConfig(navigationContext: NavigationContext<*>): NavigationHandleConfiguration<*>? {
            val pending = pendingProperties[navigationContext.contextReference.hashCode()] ?: return null
            val config = pending.get()?.config
            pendingProperties.remove(navigationContext.contextReference.hashCode())
            return config
        }
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