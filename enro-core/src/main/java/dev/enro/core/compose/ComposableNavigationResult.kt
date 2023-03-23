package dev.enro.core.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import dev.enro.core.NavigationKey
import dev.enro.core.controller.usecase.createResultChannel
import dev.enro.core.result.EnroResultChannel
import java.util.*


@Composable
public inline fun <reified T : Any> registerForNavigationResult(
    // Sometimes, particularly when interoperating between Compose and the legacy View system,
    // it may be required to provide an id explicitly. This should not be required when using
    // registerForNavigationResult from an entirely Compose-based screen.
    // Remember a random UUID that will be used to uniquely identify this result channel
    // within the composition. This is important to ensure that results are delivered if a Composable
    // is used multiple times within the same composition (such as within a list).
    // See ComposableListResultTests
    id: String = rememberSaveable {
        UUID.randomUUID().toString()
    },
    noinline onClosed: @DisallowComposableCalls () -> Unit = {},
    noinline onResult: @DisallowComposableCalls (T) -> Unit
): EnroResultChannel<T, NavigationKey.WithResult<T>> {
    val navigationHandle = navigationHandle()

    val resultChannel = remember(onResult) {
        navigationHandle.createResultChannel(
            resultType = T::class,
            onClosed = onClosed,
            onResult = onResult,
            additionalResultId = id
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