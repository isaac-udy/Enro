package dev.enro.core.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import dev.enro.core.result.EnroResult
import dev.enro.core.result.EnroResultChannel
import dev.enro.core.result.internal.ResultChannelImpl
import java.util.*


@Composable
inline fun <reified T: Any> registerForNavigationResult(
    noinline onResult: @DisallowComposableCalls (T) -> Unit
): EnroResultChannel<T> {
    val navigationHandle = navigationHandle()
    val resultId = rememberSaveable {
        UUID.randomUUID().toString()
    }
    val resultChannel = remember(onResult) {
        ResultChannelImpl(
            navigationHandle = navigationHandle,
            resultType = T::class.java,
            onResult = onResult,
            resultId = resultId
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