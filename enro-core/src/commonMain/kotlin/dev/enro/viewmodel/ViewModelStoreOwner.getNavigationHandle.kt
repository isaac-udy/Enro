package dev.enro.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.handle.NavigationHandleHolder
import kotlin.reflect.KClass

@PublishedApi
internal inline fun <reified K: NavigationKey> ViewModelStoreOwner.getNavigationHandle(): NavigationHandle<K> {
    return getNavigationHandle(K::class)
}

@PublishedApi
internal fun <K: NavigationKey> ViewModelStoreOwner.getNavigationHandle(
    keyType: KClass<K>,
): NavigationHandle<K> {
    val provider = ViewModelProvider.create(
        store = viewModelStore,
        factory = object : ViewModelProvider.Factory {}
    )
    val navigationHandle = provider[NavigationHandleHolder::class].navigationHandle
    require(keyType.isInstance(navigationHandle.key)) {
        "The NavigationHandle found in the ViewModelStoreOwner $this is not of type ${keyType.simpleName}"
    }
    @Suppress("UNCHECKED_CAST")
    return navigationHandle as NavigationHandle<K>
}
