package dev.enro.core.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import dev.enro.core.result.EnroResult
import dev.enro.core.result.EnroResultChannel
import dev.enro.core.result.internal.ResultChannelImpl


@Composable
inline fun <reified T: Any> registerForNavigationResult(
    noinline onResult: @DisallowComposableCalls (T) -> Unit
): EnroResultChannel<T> {
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