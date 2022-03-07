package dev.enro.core.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import dev.enro.core.result.EnroResultChannel
import dev.enro.core.result.internal.ResultChannelImpl
import java.util.*


@Composable
inline fun <reified T: Any> registerForNavigationResult(
    noinline onResult: @DisallowComposableCalls (T) -> Unit
): EnroResultChannel<T> {
    val navigationHandle = navigationHandle()

    // Remember a random UUID that will be used to uniquely identify this result channel
    // within the composition. This is important to ensure that results are delivered if a Composable
    // is used multiple times within the same composition (such as within a list).
    // See ComposableListResultTests
    val resultId = rememberSaveable {
        UUID.randomUUID().toString()
    }
    val resultChannel = remember(onResult) {
        ResultChannelImpl(
            navigationHandle = navigationHandle,
            resultType = T::class.java,
            onResult = onResult,
            additionalResultId = resultId
        )
    }

    DisposableEffect(true) {
        resultChannel.attach()
        onDispose {
            resultChannel.detach()
        }
    }
    return resultChannel
}