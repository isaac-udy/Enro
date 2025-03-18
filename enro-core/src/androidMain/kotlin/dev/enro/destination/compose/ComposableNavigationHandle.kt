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

/**
 * Gets the current NavigationHandle from the Composition as a TypedNavigationHandle of type T.
 *
 * This function will throw if the current NavigationHandle is not of type T, or if there is no NavigationHandle
 * available in the current Composition.
 *
 * To apply configuration to the NavigationHandle, use the [configure] function.
 */
@Composable
public inline fun <reified T : NavigationKey> navigationHandle(): TypedNavigationHandle<T> {
    val navigationHandle = navigationHandle()
    return remember(navigationHandle) {
        navigationHandle.asTyped()
    }
}

/**
 * Gets the current NavigationHandle from the Composition.
 *
 * This function will throw if there is no NavigationHandle available in the current Composition.
 *
 * To apply configuration to the NavigationHandle, use the [configure] function.
 */
@Composable
public fun navigationHandle(): NavigationHandle {
    val localNavigationHandle = LocalNavigationHandle.current
    val localViewModelStoreOwner = LocalViewModelStoreOwner.current

    return remember(localNavigationHandle, localViewModelStoreOwner) {
        localNavigationHandle ?: localViewModelStoreOwner!!.getNavigationHandle()
    }
}

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