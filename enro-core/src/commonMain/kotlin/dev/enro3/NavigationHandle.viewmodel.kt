package dev.enro3

import androidx.lifecycle.ViewModel
import dev.enro3.viewmodel.NavigationHandleProvider
import dev.enro3.viewmodel.navigationHandleReference

// TODO do we actually want this to be done with a property?
public inline fun <reified K : NavigationKey> ViewModel.navigationHandle(): NavigationHandle<K> {
    val reference = navigationHandleReference
    if (reference.navigationHandle == null) {
        reference.navigationHandle = NavigationHandleProvider.get(this::class)
    }
    val navigationHandle = reference.navigationHandle
    requireNotNull(navigationHandle) {
        "Unable to retrieve navigation handle for ViewModel ${this::class.simpleName}"
    }
    require(navigationHandle.key is K) {
        "The navigation handle key does not match the expected type. Expected ${K::class.simpleName}, but got ${navigationHandle.key::class.simpleName}"
    }
    @Suppress("UNCHECKED_CAST")
    return navigationHandle as NavigationHandle<K>
}