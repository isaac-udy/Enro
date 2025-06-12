package dev.enro.viewmodel

import androidx.lifecycle.ViewModelStoreOwner
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.handle.getNavigationHandleHolder
import kotlin.reflect.KClass

@PublishedApi
internal inline fun <reified K: NavigationKey> ViewModelStoreOwner.getNavigationHandle(): NavigationHandle<K> {
    return getNavigationHandle(K::class)
}

@PublishedApi
internal fun <K: NavigationKey> ViewModelStoreOwner.getNavigationHandle(
    keyType: KClass<K>,
): NavigationHandle<K> {
    val navigationHandle = getNavigationHandleHolder().navigationHandle
    require(keyType.isInstance(navigationHandle.key)) {
        "The NavigationHandle found in the ViewModelStoreOwner $this is not of type ${keyType.simpleName}"
    }
    @Suppress("UNCHECKED_CAST")
    return navigationHandle as NavigationHandle<K>
}
