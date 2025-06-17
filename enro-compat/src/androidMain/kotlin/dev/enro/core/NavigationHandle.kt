package dev.enro.core

import android.view.View
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.closeWithoutCallback
import dev.enro.complete
import dev.enro.open
import dev.enro.viewmodel.getNavigationHandle
import dev.enro.withMetadata
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import dev.enro.navigationHandle as androidNavigationHandle

public typealias NavigationHandle = dev.enro.NavigationHandle<out NavigationKey>
public typealias TypedNavigationHandle<T> =  dev.enro.NavigationHandle<T>

public val NavigationHandle<*>.instruction: AnyOpenInstruction
    get() = this.instance

public fun dev.enro.NavigationHandle<*>.present(key: dev.enro.core.NavigationKey.SupportsPresent) {
    open(
        key.withMetadata(
            NavigationDirection.MetadataKey,
            NavigationDirection.Present,
        )
    )
}

public fun dev.enro.NavigationHandle<*>.push(key: dev.enro.core.NavigationKey.SupportsPush) {
    open(
        key.withMetadata(
            NavigationDirection.MetadataKey,
            NavigationDirection.Push,
        )
    )
}

public fun dev.enro.NavigationHandle<*>.close() {
    closeWithoutCallback()
}

public fun dev.enro.NavigationHandle<*>.requestClose() {
    close()
}

public fun <R: Any> dev.enro.NavigationHandle<out NavigationKey.WithResult<R>>.closeWithResult(result: R) {
    complete(result)
}

public inline fun <reified T : NavigationKey> Fragment.navigationHandle() : ReadOnlyProperty<Fragment, NavigationHandle<T>> {
    return navigationHandle(T::class)
}

public fun <T : NavigationKey> Fragment.navigationHandle(
    keyType: KClass<T>
) : ReadOnlyProperty<Fragment, NavigationHandle<T>> {
    return androidNavigationHandle(keyType)
}

public inline fun <reified T : NavigationKey> ComponentActivity.navigationHandle() : ReadOnlyProperty<ComponentActivity, NavigationHandle<T>> {
    return navigationHandle(T::class)
}

public fun <T : NavigationKey> ComponentActivity.navigationHandle(
    keyType: KClass<T>
) : ReadOnlyProperty<ComponentActivity, NavigationHandle<T>> {
    return androidNavigationHandle(keyType)
}

public fun ViewModelStoreOwner.getNavigationHandle(): NavigationHandle<NavigationKey> {
    return getNavigationHandle(NavigationKey::class)
}

public fun View.getNavigationHandle(): NavigationHandle<NavigationKey>? =
    findViewTreeViewModelStoreOwner()?.getNavigationHandle()

public fun View.requireNavigationHandle(): NavigationHandle<NavigationKey> {
    if (!isAttachedToWindow) {
        error("$this is not attached to any Window, which is required to retrieve a NavigationHandle")
    }
    val viewModelStoreOwner = findViewTreeViewModelStoreOwner()
        ?: error("Could not find ViewTreeViewModelStoreOwner for $this, which is required to retrieve a NavigationHandle")
    return viewModelStoreOwner.getNavigationHandle()
}