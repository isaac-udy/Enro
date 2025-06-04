package dev.enro.core

import kotlinx.serialization.Serializable

@Serializable
internal sealed class NavigationDirection {
    @Serializable
    data object Push : NavigationDirection()
    @Serializable
    data object Present : NavigationDirection()

    internal object MetadataKey : dev.enro.NavigationKey.MetadataKey<NavigationDirection>(
        default = Push,
    )
}
