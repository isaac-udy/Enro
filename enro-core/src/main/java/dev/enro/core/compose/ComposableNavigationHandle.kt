package dev.enro.core.compose

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.enro.core.*
import dev.enro.core.internal.handle.getNavigationHandleViewModel

@Composable
public inline fun <reified T : NavigationKey> navigationHandle(): TypedNavigationHandle<T> {
    val navigationHandle = navigationHandle()
    return remember {
        navigationHandle.asTyped()
    }
}

@Composable
public fun navigationHandle(): NavigationHandle {
    val localNavigationHandle = LocalNavigationHandle.current
    val localViewModelStoreOwner = LocalViewModelStoreOwner.current

    return remember {
        localNavigationHandle ?: localViewModelStoreOwner!!.getNavigationHandleViewModel()
    }
}

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
public inline fun <reified T : NavigationKey> TypedNavigationHandle<T>.configure(configuration: LazyNavigationHandleConfiguration<T>.() -> Unit = {}) {
    remember {
        LazyNavigationHandleConfiguration(T::class)
            .apply(configuration)
            .configure(this)
        true
    }
}