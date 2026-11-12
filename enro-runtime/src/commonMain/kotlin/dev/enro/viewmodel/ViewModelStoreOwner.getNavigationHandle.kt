package dev.enro.viewmodel

import androidx.lifecycle.ViewModelStoreOwner
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.handle.getNavigationHandleHolder
import kotlin.reflect.KClass

/**
 * Returns the [NavigationHandle] of the destination that owns this `ViewModelStore`,
 * typed to [K]. Use [getNavigationHandle] from an Activity, a Fragment or a custom host.
 * A Composable calls `navigationHandle<K>()` and a ViewModel declares
 * `by navigationHandle<K>()`.
 *
 * Throws if this owner has no [NavigationHandle], or if the key of the destination is
 * not a [K].
 */
public inline fun <reified K: NavigationKey> ViewModelStoreOwner.getNavigationHandle(): NavigationHandle<K> {
    return getNavigationHandle(K::class)
}

/**
 * `ViewModelStoreOwner.getNavigationHandle<K>()` with the key type passed as a [KClass].
 */
public fun <K: NavigationKey> ViewModelStoreOwner.getNavigationHandle(
    keyType: KClass<K>,
): NavigationHandle<K> {
    val navigationHandle = getNavigationHandleHolder().navigationHandle
    require(keyType.isInstance(navigationHandle.key)) {
        "The NavigationHandle found in the ViewModelStoreOwner $this is not of type ${keyType.simpleName}"
    }
    @Suppress("UNCHECKED_CAST")
    return navigationHandle as NavigationHandle<K>
}
