package dev.enro.core.compose

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.enro.core.LazyNavigationHandleConfiguration
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationKey
import dev.enro.core.TypedNavigationHandle
import dev.enro.core.asTyped
import dev.enro.core.getNavigationHandle


@SuppressLint("ComposableNaming")
@Composable
public fun NavigationHandle.configure(configuration: LazyNavigationHandleConfiguration<NavigationKey>.() -> Unit = {}): NavigationHandle {
    return remember(configuration) {
        LazyNavigationHandleConfiguration(NavigationKey::class)
            .apply(configuration)
            .configure(this)

        return@remember this
    }
}

@SuppressLint("ComposableNaming")
@Composable
public inline fun <reified T : NavigationKey> TypedNavigationHandle<T>.configure(noinline configuration: LazyNavigationHandleConfiguration<T>.() -> Unit = {}): TypedNavigationHandle<T> {
    return remember(configuration) {
        LazyNavigationHandleConfiguration(T::class)
            .apply(configuration)
            .configure(this)

        return@remember this
    }
}