package dev.enro.core.result

import dev.enro.core.NavigationDirection
import dev.enro.core.NavigationKey
import dev.enro.result.open
import dev.enro.withMetadata

public class NavigationResultChannel<Result: Any, Key : NavigationKey>(
    private val wrapped: dev.enro.result.NavigationResultChannel<Result>
) {
    public fun push(key: NavigationKey.SupportsPush.WithResult<out Result>) {
        wrapped.open(
            key.withMetadata(NavigationDirection.MetadataKey, NavigationDirection.Push)
        )
    }

    public fun push(key: dev.enro.NavigationKey.WithMetadata<out NavigationKey.SupportsPush.WithResult<out Result>>) {
        wrapped.open(
            key.withMetadata(NavigationDirection.MetadataKey, NavigationDirection.Push)
        )
    }

    public fun present(key: NavigationKey.SupportsPresent.WithResult<out Result>) {
        wrapped.open(
            key.withMetadata(NavigationDirection.MetadataKey, NavigationDirection.Present)
        )
    }

    public fun present(key: dev.enro.NavigationKey.WithMetadata<out NavigationKey.SupportsPresent.WithResult<out Result>>) {
        wrapped.open(
            key.withMetadata(NavigationDirection.MetadataKey, NavigationDirection.Present)
        )
    }
}
