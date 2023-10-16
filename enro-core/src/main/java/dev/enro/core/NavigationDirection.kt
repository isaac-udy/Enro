package dev.enro.core

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

public sealed class NavigationDirection : Parcelable {
    @Parcelize
    @Deprecated("Please use Push or Present")
    public data object Forward : NavigationDirection()

    @Parcelize
    @Deprecated("Please use a Push or Present followed by a close")
    public data object Replace : NavigationDirection()

    @Parcelize
    public data object Push : NavigationDirection()

    @Parcelize
    public data object Present : NavigationDirection()

    @Parcelize
    public data object ReplaceRoot : NavigationDirection()

    public companion object {
        public fun defaultDirection(navigationKey: NavigationKey): NavigationDirection {
            return when (navigationKey) {
                is NavigationKey.SupportsPush -> Push
                is NavigationKey.SupportsPresent -> Present
                else -> Forward
            }
        }
    }
}