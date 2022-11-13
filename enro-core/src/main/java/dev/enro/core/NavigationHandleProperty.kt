package dev.enro.core

import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.enro.core.internal.handle.NavigationHandleViewModel
import java.lang.ref.WeakReference
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
        val navigationHandle = viewModelStoreOwner.getNavigationHandle()
        return@lazy TypedNavigationHandleImpl(navigationHandle, keyType.java)
    }

    init {
        pendingProperties[lifecycleOwner.hashCode()] = WeakReference(this)
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): TypedNavigationHandle<Key> {
        return navigationHandle
    }

    public companion object {
        internal val pendingProperties =
            mutableMapOf<Int, WeakReference<NavigationHandleProperty<*>>>()

        internal fun getPendingConfig(navigationContext: NavigationContext<*>): NavigationHandleConfiguration<*>? {
            val pending =
                pendingProperties[navigationContext.contextReference.hashCode()] ?: return null
            val config = pending.get()?.config
            pendingProperties.remove(navigationContext.contextReference.hashCode())
            return config
        }
    }
}

public inline fun <reified T : NavigationKey> ComponentActivity.navigationHandle(
    noinline config: NavigationHandleConfiguration<T>.() -> Unit = {}
): NavigationHandleProperty<T> = NavigationHandleProperty(
    lifecycleOwner = this,
    viewModelStoreOwner = this,
    configBuilder = config,
    keyType = T::class
)

public inline fun <reified T : NavigationKey> Fragment.navigationHandle(
    noinline config: NavigationHandleConfiguration<T>.() -> Unit = {}
): NavigationHandleProperty<T> = NavigationHandleProperty(
    lifecycleOwner = this,
    viewModelStoreOwner = this,
    configBuilder = config,
    keyType = T::class
)

public fun ComponentActivity.getNavigationHandle(): NavigationHandle {
    return navigationContext.getNavigationHandle()
}

public fun Fragment.getNavigationHandle(): NavigationHandle {
    return navigationContext.getNavigationHandle()
}

internal fun ViewModelStoreOwner.getNavigationHandle(): NavigationHandle {
    return ExpectExistingNavigationHandleViewModelFactory(this).get()
}

public fun NavigationContext<*>.getNavigationHandle(): NavigationHandle {
    return viewModelStoreOwner.getNavigationHandle()
}

@PublishedApi
internal fun ViewModel.getNavigationHandle(): NavigationHandle {
    return requireNotNull(getNavigationHandleTag())
}

@Composable
public inline fun <reified T : NavigationKey> navigationHandle(): TypedNavigationHandle<T> {
    val navigationHandle = navigationHandle()
    return remember {
        navigationHandle.asTyped()
    }
}

public val LocalNavigationHandle: ProvidableCompositionLocal<NavigationHandle?> =
    compositionLocalOf {
        null
    }

@Composable
public fun navigationHandle(): NavigationHandle {
    val localNavigationHandle = LocalNavigationHandle.current
    val localViewModelStoreOwner = LocalViewModelStoreOwner.current

    return remember {
        localNavigationHandle ?: localViewModelStoreOwner!!.getNavigationHandle()
    }
}

public fun View.getNavigationHandle(): NavigationHandle? =
    findViewTreeViewModelStoreOwner()?.getNavigationHandle()

public fun View.requireNavigationHandle(): NavigationHandle {
    if (!isAttachedToWindow) {
        throw EnroException.InvalidViewForNavigationHandle("$this is not attached to any Window, which is required to retrieve a NavigationHandle")
    }

    val viewModelStoreOwner = findViewTreeViewModelStoreOwner()
        ?: throw EnroException.InvalidViewForNavigationHandle("Could not find ViewTreeViewModelStoreOwner for $this, which is required to retrieve a NavigationHandle")

    return viewModelStoreOwner.getNavigationHandle()
}

@ArchitectureException("We need at least one way to get the NavigationHandleViewModel from a ViewModelStoreOwner at this time")
private class ExpectExistingNavigationHandleViewModelFactory(
    private val viewModelStoreOwner: ViewModelStoreOwner
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val name = viewModelStoreOwner::class.java.simpleName
        throw EnroException.NoAttachedNavigationHandle(
            "Attempted to get the NavigationHandle for $name, but $name not have a NavigationHandle attached."
        )
    }

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val name = viewModelStoreOwner::class.java.simpleName
        throw EnroException.NoAttachedNavigationHandle(
            "Attempted to get the NavigationHandle for $name, but $name not have a NavigationHandle attached."
        )
    }

    fun get(): NavigationHandle {
        return ViewModelLazy(
            viewModelClass = NavigationHandleViewModel::class,
            storeProducer = { viewModelStoreOwner.viewModelStore },
            factoryProducer = { this }
        ).value
    }
}