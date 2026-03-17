package dev.enro.core.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.enro.NavigationKey
import dev.enro.core.result.NavigationResultChannel
import dev.enro.result.NavigationResultScope

@Composable
public inline fun <reified R : Any> registerForNavigationResult(
    noinline onClosed: NavigationResultScope<out NavigationKey.WithResult<out R>>.() -> Unit = {},
    noinline onResult: NavigationResultScope<out NavigationKey.WithResult<out R>>.(R) -> Unit,
): NavigationResultChannel<R, *> {
    val channel = dev.enro.result.registerForNavigationResult(
        onClosed = onClosed,
        onCompleted = onResult,
    )
    return remember(channel) {
        NavigationResultChannel<R, dev.enro.core.NavigationKey>(channel)
    }
}
