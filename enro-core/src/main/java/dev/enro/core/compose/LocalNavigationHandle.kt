package dev.enro.core.compose

import androidx.compose.runtime.*
import dev.enro.core.NavigationHandle
import dev.enro.core.NavigationKey
import dev.enro.core.TypedNavigationHandle
import dev.enro.core.asTyped
import dev.enro.core.result.EnroResult
import dev.enro.core.result.EnroResultChannel
import dev.enro.core.result.internal.ResultChannelImpl

val LocalNavigationHandle = compositionLocalOf<NavigationHandle> {
    throw IllegalStateException("The current composition does not have a NavigationHandle attached")
}

@Composable
inline fun <reified T: NavigationKey> navigationHandle(): TypedNavigationHandle<T> {
    return LocalNavigationHandle.current.asTyped()
}

@Composable
inline fun <reified T: Any> registerForNavigationResult(
    noinline onResult: @DisallowComposableCalls  (T) -> Unit
):  EnroResultChannel<T> {
    val navigationHandle = LocalNavigationHandle.current
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

@Composable
fun navigationHandle(): NavigationHandle {
    return LocalNavigationHandle.current
}