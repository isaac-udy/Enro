package dev.enro.core.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationKey
import dev.enro.core.TypedNavigationHandle
import dev.enro.core.asTyped
import dev.enro.core.internal.handle.getNavigationHandleViewModel

val LocalNavigationHandle = compositionLocalOf<NavigationHandle?> {
    null
}

@Composable
inline fun <reified T: NavigationKey> navigationHandle(): TypedNavigationHandle<T> {
    return navigationHandle().asTyped()
}

@Composable
fun navigationHandle(): NavigationHandle {
    return LocalNavigationHandle.current ?: LocalViewModelStoreOwner.current!!.getNavigationHandleViewModel()
}