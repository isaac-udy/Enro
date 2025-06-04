package dev.enro

import androidx.lifecycle.ViewModel
import dev.enro.viewmodel.NavigationHandleProvider
import dev.enro.viewmodel.navigationHandleReference
import kotlin.properties.ReadOnlyProperty

// TODO do we actually want this to be done with a property?
public inline fun <reified K : NavigationKey> ViewModel.navigationHandle(
    noinline config: (NavigationHandleConfiguration<K>.() -> Unit)? = null,
): ReadOnlyProperty<ViewModel, NavigationHandle<K>> {
    val navigationHandle = getNavigationHandle()
    require(navigationHandle.key is K) {
        "The navigation handle key does not match the expected type. Expected ${K::class.simpleName}, but got ${navigationHandle.key::class.simpleName}"
    }

    if (config != null) {
        @Suppress("UNCHECKED_CAST")
        val configuration = NavigationHandleConfiguration(navigationHandle)
            .apply(config as NavigationHandleConfiguration<NavigationKey>.() -> Unit)
        addCloseable(AutoCloseable { configuration.close() })
    }

    @Suppress("UNCHECKED_CAST")
    return ReadOnlyProperty { _, _ -> navigationHandle as NavigationHandle<K> }
}

public inline fun <reified K : NavigationKey> ViewModel.navigationHandle(): ReadOnlyProperty<ViewModel, NavigationHandle<K>> {
   return navigationHandle(
       config = null,
   )
}

public fun ViewModel.getNavigationHandle(): NavigationHandle<NavigationKey> {
    val reference = navigationHandleReference
    if (reference.navigationHandle == null) {
        reference.navigationHandle = NavigationHandleProvider.get(this::class)
    }
    val navigationHandle = reference.navigationHandle
    requireNotNull(navigationHandle) {
        "Unable to retrieve navigation handle for ViewModel ${this::class.simpleName}"
    }
    return navigationHandle
}
