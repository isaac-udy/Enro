package dev.enro.core

import android.app.Activity
import android.view.View
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import dev.enro.core.internal.handle.getNavigationHandleViewModel
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
        return@lazy TypedNavigationHandleImpl(navigationHandle, keyType.java)
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

inline fun <reified T: NavigationKey> ComponentActivity.navigationHandle(
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

fun NavigationContext<*>.getNavigationHandle(): NavigationHandle = getNavigationHandleViewModel()

fun ComponentActivity.getNavigationHandle(): NavigationHandle = getNavigationHandleViewModel()

fun Fragment.getNavigationHandle(): NavigationHandle = getNavigationHandleViewModel()

fun View.getNavigationHandle(): NavigationHandle? = findViewTreeViewModelStoreOwner()?.getNavigationHandleViewModel()

fun View.requireNavigationHandle(): NavigationHandle {
    if(!isAttachedToWindow) {
        throw EnroException.InvalidViewForNavigationHandle("$this is not attached to any Window, which is required to retrieve a NavigationHandle")
    }
    val viewModelStoreOwner = findViewTreeViewModelStoreOwner()
        ?: throw EnroException.InvalidViewForNavigationHandle("Could not find ViewTreeViewModelStoreOwner for $this, which is required to retrieve a NavigationHandle")
    return viewModelStoreOwner.getNavigationHandleViewModel()
}