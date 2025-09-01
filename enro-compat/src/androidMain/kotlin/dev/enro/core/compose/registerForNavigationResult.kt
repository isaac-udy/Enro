package dev.enro.core.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.enro.NavigationKey
import dev.enro.core.NavigationDirection
import dev.enro.result.NavigationResultChannel
import dev.enro.result.NavigationResultScope
import dev.enro.result.open
import dev.enro.withMetadata

@Composable
public inline fun <reified R : Any> registerForNavigationResult(
    noinline onClosed: NavigationResultScope<out NavigationKey.WithResult<out R>>.() -> Unit = {},
    noinline onResult: NavigationResultScope<out NavigationKey.WithResult<out R>>.(R) -> Unit,
): NavigationResultChannelCompat<R> {
    val channel = dev.enro.result.registerForNavigationResult(
        onClosed = onClosed,
        onCompleted = onResult,
    )
    return remember(channel) {
        NavigationResultChannelCompat(channel)
    }
}

public class NavigationResultChannelCompat<R : Any>(
    private val channel: NavigationResultChannel<R>
) {
    public fun push(
        key: dev.enro.core.NavigationKey.SupportsPush.WithResult<out R>,
    ) {
        channel.open(
            key.withMetadata(NavigationDirection.MetadataKey, NavigationDirection.Push)
        )
    }

    public fun present(
        key: dev.enro.core.NavigationKey.SupportsPresent.WithResult<out R>,
    ) {
        channel.open(
            key.withMetadata(NavigationDirection.MetadataKey, NavigationDirection.Present)
        )
    }
}
