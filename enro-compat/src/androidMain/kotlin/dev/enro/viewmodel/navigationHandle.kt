package dev.enro.viewmodel

import androidx.lifecycle.ViewModel
import dev.enro.NavigationHandle
import dev.enro.NavigationHandleConfiguration
import dev.enro.NavigationKey
import kotlin.properties.ReadOnlyProperty
import dev.enro.navigationHandle as realNavigationHandle


public inline fun <reified K : NavigationKey> ViewModel.navigationHandle(
    noinline config: (NavigationHandleConfiguration<K>.() -> Unit)? = null,
): ReadOnlyProperty<ViewModel, NavigationHandle<K>> {
    return realNavigationHandle(config)
}

public inline fun <reified K : NavigationKey> ViewModel.navigationHandle(): ReadOnlyProperty<ViewModel, NavigationHandle<K>> {
    return navigationHandle(config = null)
}