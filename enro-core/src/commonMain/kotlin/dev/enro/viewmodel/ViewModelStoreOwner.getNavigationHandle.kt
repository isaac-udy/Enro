package dev.enro.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dev.enro.NavigationHandle
import dev.enro.NavigationKey
import dev.enro.handle.NavigationHandleHolder

@PublishedApi
internal inline fun <reified K: NavigationKey> ViewModelStoreOwner.getNavigationHandle(): NavigationHandle<K> {
    val provider = ViewModelProvider.create(
        store = viewModelStore,
        factory = object : ViewModelProvider.Factory {}
    )
    val navigationHandle = provider[NavigationHandleHolder::class].navigationHandle
    require(navigationHandle.key is K) {
        "The NavigationHandle found in the ViewModelStoreOwner $this is not of type ${K::class.simpleName}"
    }

    @Suppress("UNCHECKED_CAST")
    return navigationHandle as NavigationHandle<K>
}
