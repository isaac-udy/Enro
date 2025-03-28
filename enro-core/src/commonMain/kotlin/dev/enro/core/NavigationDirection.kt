package dev.enro.core

import dev.enro.core.internal.EnroSerializable

public sealed class NavigationDirection : EnroSerializable {
    @Deprecated("Please use Push or Present")
    public data object Forward : NavigationDirection()

    @Deprecated("Please use a Push or Present followed by a close")
    public data object Replace : NavigationDirection()

    public data object Push : NavigationDirection()

    public data object Present : NavigationDirection()

    public data object ReplaceRoot : NavigationDirection()

    public companion object
}
