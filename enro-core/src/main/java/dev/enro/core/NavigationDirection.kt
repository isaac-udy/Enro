package dev.enro.core

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

public sealed class NavigationDirection : Parcelable {
    @Parcelize
    @Deprecated("Please use Push or Present")
    public object Forward : NavigationDirection()

    @Parcelize
    @Deprecated("Please use a close instruction followed by a Push or Present")
    public object Replace : NavigationDirection()

    @Parcelize
    public object Push : NavigationDirection()

    @Parcelize
    public object Present : NavigationDirection()

    @Parcelize
    public object ReplaceRoot : NavigationDirection()

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