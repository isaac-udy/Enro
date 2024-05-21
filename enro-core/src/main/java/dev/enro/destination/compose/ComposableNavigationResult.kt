package dev.enro.core.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import dev.enro.core.NavigationKey
import dev.enro.core.controller.usecase.createResultChannel
import dev.enro.core.result.NavigationResultChannel
import dev.enro.core.result.NavigationResultScope
import java.util.UUID


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
    noinline onClosed: @DisallowComposableCalls NavigationResultScope<T, NavigationKey.WithResult<T>>.() -> Unit = {},
    noinline onResult: @DisallowComposableCalls NavigationResultScope<T, NavigationKey.WithResult<T>>.(T) -> Unit
): NavigationResultChannel<T, NavigationKey.WithResult<T>> {
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
        // In some cases, particularly with navigation to Activities,
        // Composables aren't actually called through to onDispose, meaning the
        // result channel sticks around as being "active" even though the associated
        // activity is not started. We're adding a lifecycle observer here to ensure this
        // is managed correctly.
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                resultChannel.attach()
            }
            if (event == Lifecycle.Event.ON_STOP) {
                resultChannel.detach()
            }
        }
        navigationHandle.lifecycle.addObserver(observer)
        if (navigationHandle.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            resultChannel.attach()
        }
        onDispose {
            navigationHandle.lifecycle.removeObserver(observer)
            resultChannel.detach()
        }
    }
    return resultChannel
}