package dev.enro.core.compose

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationKey
import dev.enro.core.TypedNavigationHandle
import dev.enro.core.asTyped
import dev.enro.core.internal.handle.getNavigationHandleViewModel
import dev.enro.core.result.EnroResult
import dev.enro.core.result.EnroResultChannel
import dev.enro.core.result.internal.ResultChannelImpl

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

@Composable
inline fun <reified T: Any> registerForNavigationResult(
    noinline onResult: @DisallowComposableCalls  (T) -> Unit
):  EnroResultChannel<T> {
    val navigationHandle = navigationHandle()
    val resultChannel = remember {
        ResultChannelImpl(
            navigationHandle = navigationHandle,
            resultType = T::class.java,
            onResult = onResult
        )
    }

    DisposableEffect(true) {
        EnroResult.from(navigationHandle.controller).registerChannel(resultChannel)
        onDispose {
            EnroResult.from(navigationHandle.controller).deregisterChannel(resultChannel)
        }
    }
    return resultChannel
}