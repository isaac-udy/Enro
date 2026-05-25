package dev.enro.viewmodel

import androidx.lifecycle.ViewModelStoreOwner
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.handle.getNavigationHandleHolder
import kotlin.reflect.KClass

/**
 * Returns the [NavigationHandle] typed to [K] for the destination whose
 * `ViewModelStore` this owner exposes.
 *
 * Intended for Android-side glue (Activity, Fragment, custom hosts) that
 * needs to reach the handle for a destination it owns. Inside the
 * destination's Composable or ViewModel, prefer `navigationHandle<K>()`
 * (composable) or `by navigationHandle<K>()` (ViewModel) — those are the
 * standard accessors.
 *
 * Throws if no Enro handle is associated with this owner, or if [K]
 * doesn't match the destination's actual key type.
 */
public inline fun <reified K: NavigationKey> ViewModelStoreOwner.getNavigationHandle(): NavigationHandle<K> {
    return getNavigationHandle(K::class)
}

/**
 * Explicit-[KClass] form of `ViewModelStoreOwner.getNavigationHandle<K>()`.
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
