package dev.enro.compose

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.enro.core.LazyNavigationHandleConfiguration
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationKey
import dev.enro.core.TypedNavigationHandle

@SuppressLint("ComposableNaming")
@Composable
public fun NavigationHandle.configure(configuration: LazyNavigationHandleConfiguration<NavigationKey>.() -> Unit = {}) {
    remember {
        LazyNavigationHandleConfiguration(NavigationKey::class)
            .apply(configuration)
            .configure(this)
        true
    }
}

@SuppressLint("ComposableNaming")
@Composable
public inline fun <reified T : NavigationKey> TypedNavigationHandle<T>.configure(crossinline configuration: LazyNavigationHandleConfiguration<T>.() -> Unit = {}) {
    remember {
        LazyNavigationHandleConfiguration(T::class)
            .apply(configuration)
            .configure(this)
        true
    }
}