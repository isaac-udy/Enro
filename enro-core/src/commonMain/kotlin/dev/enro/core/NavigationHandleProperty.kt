package dev.enro.core

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import dev.enro.core.internal.EnroWeakReference
import dev.enro.core.internal.handle.NavigationHandleViewModel
import dev.enro.core.internal.handle.getNavigationHandleViewModel
import kotlin.collections.set
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


public class NavigationHandleProperty<Key : NavigationKey> @PublishedApi internal constructor(
    private val lifecycleOwner: LifecycleOwner,
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val configBuilder: NavigationHandleConfiguration<Key>.() -> Unit = {},
    private val keyType: KClass<Key>
) : ReadOnlyProperty<Any, TypedNavigationHandle<Key>> {

    private val config = NavigationHandleConfiguration(keyType).apply(configBuilder)

    private val navigationHandle: TypedNavigationHandle<Key> by lazy {
        val navigationHandle = viewModelStoreOwner.getNavigationHandleViewModel()
        return@lazy TypedNavigationHandleImpl(navigationHandle, keyType)
    }

    init {
        pendingProperties[lifecycleOwner.hashCode()] = EnroWeakReference(this)
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): TypedNavigationHandle<Key> {
        return navigationHandle
    }

    public companion object {
        internal val pendingProperties =
            mutableMapOf<Int, EnroWeakReference<NavigationHandleProperty<*>>>()

        internal fun getPendingConfig(navigationContext: NavigationContext<*>): NavigationHandleConfiguration<*>? {
            val pending =
                pendingProperties[navigationContext.contextReference.hashCode()] ?: return null
            val config = pending.get()?.config
            pendingProperties.remove(navigationContext.contextReference.hashCode())
            return config
        }
    }
}

public fun NavigationContext<*>.getNavigationHandle(): NavigationHandle =
    viewModelStoreOwner.getNavigationHandle()

internal fun NavigationHandle.getNavigationContext(): NavigationContext<*>? {
    val navigationHandle = this
    val unwrapped = when(navigationHandle) {
        is TypedNavigationHandleImpl<*> -> navigationHandle.navigationHandle
        else -> navigationHandle
    }

    return when(unwrapped) {
        is NavigationHandleViewModel -> unwrapped.navigationContext
        else -> null
    }
}

internal fun NavigationHandle.requireNavigationContext(): NavigationContext<*> {
    return requireNotNull(getNavigationContext())
}